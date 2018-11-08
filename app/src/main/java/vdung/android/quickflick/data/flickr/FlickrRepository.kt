package vdung.android.quickflick.data.flickr

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import org.reactivestreams.Publisher
import javax.inject.Inject

class FlickrRepository @Inject constructor(
    private val service: FlickrService
) {

    fun getInterestingPhotos(): Publisher<PagedList<FlickrPhoto>> {
        val factory: DataSource.Factory<Int, FlickrPhoto> = object : DataSource.Factory<Int, FlickrPhoto>() {
            override fun create(): DataSource<Int, FlickrPhoto> {
                return object : FlickrPhotosDataSource() {
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
                }
            }
        }

        return RxPagedListBuilder(factory, 100).buildFlowable(BackpressureStrategy.LATEST)
    }
}