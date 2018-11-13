package vdung.android.quickflick.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import io.reactivex.Flowable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.*
import org.reactivestreams.Publisher
import vdung.android.quickflick.data.Result
import vdung.android.quickflick.data.ResultPublisher
import vdung.android.quickflick.data.flickr.FlickrPhoto
import vdung.android.quickflick.data.flickr.FlickrRepository
import vdung.android.quickflick.data.flickr.FlickrSearch
import vdung.android.quickflick.data.flickr.FlickrTag
import vdung.android.quickflick.mock
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class MainViewModelTest {

    private lateinit var repository: FlickrRepository
    private lateinit var testScheduler: TestScheduler

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            Schedulers.trampoline()
        }

        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler {
            testScheduler
        }

        RxJavaPlugins.setIoSchedulerHandler {
            Schedulers.trampoline()
        }

        repository = mock(FlickrRepository::class.java).apply {
            `when`(interestingPhotos).thenReturn(mock())
            `when`(createRelatedTagsPublisher()).thenReturn(mock())
        }
    }

    @Test
    fun testRefresh() {
        val mockPhotos = mock<ResultPublisher<PagedList<FlickrPhoto>, FlickrSearch, Unit>>()
        `when`(repository.interestingPhotos).thenReturn(mockPhotos)

        val viewModel = MainViewModel(repository)
        viewModel.refresh()

        verify(mockPhotos, times(2)).fetch(FlickrSearch("", emptyList()))
        verifyNoMoreInteractions(mockPhotos)
    }

    @Test
    fun tesTagChanged() {
        val mockPhotos = mock<ResultPublisher<PagedList<FlickrPhoto>, FlickrSearch, Unit>>()
        `when`(repository.interestingPhotos).thenReturn(mockPhotos)

        val viewModel = MainViewModel(repository)
        val tag = FlickrTag("foo")
        viewModel.tagChanged(tag, true)
        viewModel.tagChanged(tag, false)

        inOrder(mockPhotos).apply {
            verify(mockPhotos).fetch(FlickrSearch("", emptyList()))
            verify(mockPhotos).fetch(FlickrSearch("", listOf(tag)))
            verify(mockPhotos).fetch(FlickrSearch("", emptyList()))
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun testQueryTextSubmitted() {
        val mockPhotos = mock<ResultPublisher<PagedList<FlickrPhoto>, FlickrSearch, Unit>>()
        `when`(repository.interestingPhotos).thenReturn(mockPhotos)

        val viewModel = MainViewModel(repository)
        viewModel.queryTextSubmitted("foo")
        viewModel.queryTextSubmitted("bar")

        inOrder(mockPhotos).apply {
            verify(mockPhotos).fetch(FlickrSearch("", emptyList()))
            verify(mockPhotos).fetch(FlickrSearch("foo", emptyList()))
            verify(mockPhotos).fetch(FlickrSearch("bar", emptyList()))
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun testQueryTextChanged() {
        val mockTags = mock<ResultPublisher<List<FlickrTag>, String, List<FlickrTag>>>()
        `when`(repository.createRelatedTagsPublisher()).thenReturn(mockTags)

        val viewModel = MainViewModel(repository)
        viewModel.queryTextChanged("foo")

        viewModel.queryTextChanged("bar")
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        viewModel.queryTextChanged("baz")
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        inOrder(mockTags).apply {
            verify(mockTags).fetch("bar")
            verify(mockTags).fetch("baz")
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun testSuggestionClicked() {
        val mockPhotos = mock<ResultPublisher<PagedList<FlickrPhoto>, FlickrSearch, Unit>>()
        val data = PublishProcessor.create<List<FlickrTag>>()
        val testTags = object : ResultPublisher<List<FlickrTag>, String, List<FlickrTag>>() {
            override fun localData(): Publisher<List<FlickrTag>> {
                return data
            }

            override fun shouldFetch(arg: String, previousResult: List<FlickrTag>): Boolean {
                return true
            }

            override fun fetchFromNetwork(arg: String): Publisher<List<FlickrTag>> {
                return Flowable.never()
            }
        }
        `when`(repository.interestingPhotos).thenReturn(mockPhotos)
        `when`(repository.createRelatedTagsPublisher()).thenReturn(testTags)

        val viewModel = MainViewModel(repository)

        val mockObserver = mock<Observer<Result<List<FlickrTag>>>>()
        viewModel.relatedTags.observeForever(mockObserver)

        data.onNext(listOf(FlickrTag("foo"), FlickrTag("bar")))

        viewModel.suggestionClicked(0)
        viewModel.suggestionClicked(1)

        verify(mockObserver).onChanged(Result.Success(listOf(FlickrTag("foo"), FlickrTag("bar"))))

        inOrder(mockPhotos).apply {
            verify(mockPhotos).fetch(FlickrSearch("", emptyList()))
            verify(mockPhotos).fetch(FlickrSearch("", listOf(FlickrTag("foo"))))
            verify(mockPhotos).fetch(FlickrSearch("", listOf(FlickrTag("foo"), FlickrTag("bar"))))
            verifyNoMoreInteractions()
        }
    }
}
