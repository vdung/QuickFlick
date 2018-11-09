package vdung.android.quickflick.data.flickr

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import vdung.android.quickflick.data.ResultPublisher
import javax.inject.Inject

class FlickrRepository @Inject constructor(
    private val service: FlickrService
) {

    val interestingPhotos: ResultPublisher<PagedList<FlickrPhoto>, Unit, Unit> by lazy {
        return@lazy object : ResultPublisher<PagedList<FlickrPhoto>, Unit, Unit>() {
            override fun localData(): Publisher<PagedList<FlickrPhoto>> {
                return createInterestingPhotosPublisher()
            }

            override fun shouldFetch(arg: Unit, previousResult: PagedList<FlickrPhoto>): Boolean {
                previousResult.dataSource.invalidate()
                return true
            }

            override fun fetchFromNetwork(arg: Unit): Publisher<Unit> {
                return Flowable.never()
            }
        }
    }

    private fun createInterestingPhotosPublisher(): Publisher<PagedList<FlickrPhoto>> {
        val factory: DataSource.Factory<Int, FlickrPhoto> = object : DataSource.Factory<Int, FlickrPhoto>() {
            override fun create(): DataSource<Int, FlickrPhoto> {
                return object : FlickrPhotosDataSource() {
                    override fun loadInitialRequest(params: LoadInitialParams<Int>) =
                        service.getInterestingPhotos(
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
                }
            }
        }

        return RxPagedListBuilder<Int, FlickrPhoto>(factory, 100).buildFlowable(BackpressureStrategy.LATEST)
    }
}