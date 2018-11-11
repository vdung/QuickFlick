package vdung.android.quickflick.data.flickr

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import org.reactivestreams.Publisher
import vdung.android.quickflick.data.ResultPublisher
import javax.inject.Inject

data class FlickrSearch(
    val text: String,
    val tags: List<FlickrTag>
) {
    companion object {
        val EMPTY = FlickrSearch("", emptyList())
    }
}

class FlickrSearchPhotosDataSource(
    private val service: FlickrService,
    private val args: FlickrSearch,
    private val errorHandler: (Throwable) -> Unit
) :
    FlickrPhotosDataSource() {
    override fun loadInitialRequest(params: LoadInitialParams<Int>) = service.searchPhotos(
        page = 1,
        perPage = params.requestedLoadSize,
        text = args.text,
        tags = args.tags.joinToString(separator = ",") { it.content },
        extras = FlickrExtras.allValues()
    )

    override fun loadBeforeRequest(params: LoadParams<Int>) = service.searchPhotos(
        page = params.key,
        perPage = params.requestedLoadSize,
        text = args.text,
        tags = args.tags.joinToString(separator = ",") { it.content },
        extras = FlickrExtras.allValues()
    )

    override fun loadAfterRequest(params: LoadParams<Int>) = service.searchPhotos(
        page = params.key,
        perPage = params.requestedLoadSize,
        text = args.text,
        tags = args.tags.joinToString(separator = ",") { it.content },
        extras = FlickrExtras.allValues()
    )

    override fun onError(error: Throwable) {
        errorHandler(error)
    }

    class Factory(
        private val service: FlickrService,
        private val args: FlickrSearch,
        private val errorHandler: (Throwable) -> Unit
    ) :
        DataSource.Factory<Int, FlickrPhoto>() {
        override fun create(): DataSource<Int, FlickrPhoto> {
            return FlickrSearchPhotosDataSource(service, args, errorHandler)
        }
    }
}

class FlickrInterestingPhotosDataSource(
    private val service: FlickrService,
    private val errorHandler: (Throwable) -> Unit
) : FlickrPhotosDataSource() {
    override fun loadInitialRequest(params: LoadInitialParams<Int>) = service.getInterestingPhotos(
        page = 1,
        perPage = params.requestedLoadSize,
        extras = FlickrExtras.allValues()
    )

    override fun loadBeforeRequest(params: LoadParams<Int>) = service.getInterestingPhotos(
        page = params.key,
        perPage = params.requestedLoadSize,
        extras = FlickrExtras.allValues()
    )

    override fun loadAfterRequest(params: LoadParams<Int>) = service.getInterestingPhotos(
        page = params.key,
        perPage = params.requestedLoadSize,
        extras = FlickrExtras.allValues()
    )

    override fun onError(error: Throwable) {
        errorHandler(error)
    }

    class Factory(private val service: FlickrService, private val errorHandler: (Throwable) -> Unit) :
        DataSource.Factory<Int, FlickrPhoto>() {
        override fun create(): DataSource<Int, FlickrPhoto> {
            return FlickrInterestingPhotosDataSource(service, errorHandler)
        }
    }
}

class FlickrRepository @Inject constructor(
    private val service: FlickrService
) {

    val interestingPhotos: ResultPublisher<PagedList<FlickrPhoto>, FlickrSearch, Unit> by lazy {
        return@lazy object : ResultPublisher<PagedList<FlickrPhoto>, FlickrSearch, Unit>() {
            private val searchArgs = BehaviorProcessor.createDefault<FlickrSearch>(FlickrSearch.EMPTY)
            private val errorProcessor = PublishProcessor.create<Throwable>()

            override fun localData(): Publisher<PagedList<FlickrPhoto>> {
                return searchArgs.switchMap {
                    return@switchMap if (it == FlickrSearch.EMPTY) {
                        createInterestingPhotosPublisher()
                    } else {
                        createSearchPhotosPublisher(it)
                    }
                }
            }

            override fun shouldFetch(arg: FlickrSearch, previousResult: PagedList<FlickrPhoto>): Boolean {
                searchArgs.onNext(arg)
                return true
            }

            override fun fetchFromNetwork(arg: FlickrSearch): Publisher<Unit> {
                return errorProcessor.take(1).flatMap { Flowable.error<Unit>(it) }
            }

            fun createInterestingPhotosPublisher(): Publisher<PagedList<FlickrPhoto>> {
                val factory = FlickrInterestingPhotosDataSource.Factory(service) {
                    errorProcessor.onNext(it)
                }
                return RxPagedListBuilder<Int, FlickrPhoto>(factory, getDefaultDataSourceConfig()).buildFlowable(
                    BackpressureStrategy.LATEST
                )
            }

            fun createSearchPhotosPublisher(args: FlickrSearch): Publisher<PagedList<FlickrPhoto>> {
                val factory = FlickrSearchPhotosDataSource.Factory(service, args) {
                    errorProcessor.onNext(it)
                }
                return RxPagedListBuilder<Int, FlickrPhoto>(factory, getDefaultDataSourceConfig()).buildFlowable(
                    BackpressureStrategy.LATEST
                )
            }
        }
    }

    private fun getDefaultDataSourceConfig() = PagedList.Config.Builder()
        .setPageSize(50)
        .setPrefetchDistance(150)
        .setEnablePlaceholders(true)
        .build()
}