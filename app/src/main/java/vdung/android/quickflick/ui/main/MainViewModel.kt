package vdung.android.quickflick.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import androidx.paging.PagedList
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import vdung.android.quickflick.data.Result
import vdung.android.quickflick.data.flickr.FlickrPhoto
import vdung.android.quickflick.data.flickr.FlickrRepository
import vdung.android.quickflick.data.flickr.FlickrSearch
import vdung.android.quickflick.data.flickr.FlickrTag
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.min

sealed class ChipTag {
    data class Added(val tag: FlickrTag) : ChipTag()
    data class Suggested(val tag: FlickrTag) : ChipTag()
    data class Query(val text: String) : ChipTag()
}

class MainViewModel @Inject constructor(
    flickrRepository: FlickrRepository
) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val checkedTagEvent = PublishProcessor.create<Pair<FlickrTag, Boolean>>()
    private val checkedTags = checkedTagEvent
        .scan(emptyList<FlickrTag>()) { tags, changedTag ->
            val checked = changedTag.second
            if (checked) {
                return@scan tags + listOf(changedTag.first)
            } else {
                return@scan tags.filterNot { it == changedTag.first }
            }
        }
        .replay(1)
        .refCount()

    private val queryText = BehaviorProcessor.createDefault("")

    private val relatedTagsQuery = PublishProcessor.create<String>()
    private val relatedTagsResult = flickrRepository.createRelatedTagsPublisher()

    private val searchArgs = Flowable
        .combineLatest<List<FlickrTag>, String, FlickrSearch>(
            checkedTags,
            queryText,
            BiFunction { tags, text -> FlickrSearch(text, tags) }
        )
        .replay(1)
        .refCount()

    private val interestingPhotos = flickrRepository.interestingPhotos
    private val tagsOfFetchedPhotos = Flowable.fromPublisher(interestingPhotos)
        .observeOn(Schedulers.computation())
        .switchMap { result ->
            return@switchMap when (result) {
                is Result.Success -> extractTags(result.result)
                else -> Flowable.just(emptyList())
            }
        }
        .observeOn(AndroidSchedulers.mainThread())

    private fun extractTags(photoList: PagedList<FlickrPhoto>): Flowable<List<FlickrTag>> {
        return Flowable.create<List<FlickrTag>>({ emitter ->
            val callback = object : PagedList.Callback() {
                override fun onChanged(position: Int, count: Int) {
                    emitTags()
                }

                override fun onInserted(position: Int, count: Int) {
                    emitTags()
                }

                override fun onRemoved(position: Int, count: Int) {
                    emitTags()
                }

                fun emitTags() {
                    photoList
                        .take(min(photoList.loadedCount, 100))
                        .flatMap { it.tags.split(Regex("\\s")) }
                        .filter { it.isNotEmpty() }
                        .groupingBy { it }
                        .eachCount()
                        .map { FlickrTag(it.key, it.value) }
                        .sortedByDescending { it.score }
                        .let { emitter.onNext(it) }
                }
            }

            photoList.addWeakCallback(null, callback)
            emitter.setDisposable(Disposables.fromAction {
                photoList.removeWeakCallback(callback)
            })

            callback.emitTags()
        }, BackpressureStrategy.LATEST)
    }

    val photos = interestingPhotos.toLiveData()
    val tags = tagsOfFetchedPhotos
        .withLatestFrom(
            queryText,
            checkedTags,
            Function3<List<FlickrTag>, String, List<FlickrTag>, List<ChipTag>> { fetchedTags, text, checkedTags ->
                val checkedTagNames = listOf(text) + checkedTags.map { it.content }
                val allTags = mutableListOf<ChipTag>()
                if (text != "") {
                    allTags.add(ChipTag.Query(text))
                }

                allTags.addAll(checkedTags.map { ChipTag.Added(it) })
                allTags.addAll(fetchedTags.filter { !checkedTagNames.contains(it.content) }.take(20).map {
                    ChipTag.Suggested(it)
                })

                return@Function3 allTags
            }
        )
        .toLiveData()

    val isRefreshing = Flowable.fromPublisher(interestingPhotos).map { it is Result.Pending }.toLiveData()

    val relatedTags = Flowable.fromPublisher(relatedTagsResult)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .toLiveData()

    init {
        searchArgs.subscribe { interestingPhotos.fetch(it) }
            .also { disposable.add(it) }
        relatedTagsQuery.debounce(1, TimeUnit.SECONDS).subscribe { relatedTagsResult.fetch(it) }
            .also { disposable.add(it) }
    }

    override fun onCleared() {
        disposable.dispose()
    }

    fun refresh() {
        interestingPhotos.fetch(searchArgs.blockingFirst())
    }

    fun tagChanged(tag: FlickrTag, isChecked: Boolean) {
        checkedTagEvent.onNext(tag to isChecked)
    }

    fun queryTextSubmitted(query: String?) {
        if (query == null) {
            return
        }

        queryText.onNext(query)
    }

    fun queryTextChanged(query: String?) {
        relatedTagsQuery.onNext(query ?: "")
    }

    fun suggestionClicked(position: Int) {
        val tag = relatedTags.value?.value?.get(position) ?: return

        checkedTagEvent.onNext(tag to true)
    }
}