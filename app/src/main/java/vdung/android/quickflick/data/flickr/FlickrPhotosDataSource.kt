package vdung.android.quickflick.data.flickr

import androidx.paging.PageKeyedDataSource
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

abstract class FlickrPhotosDataSource : PageKeyedDataSource<Int, FlickrPhoto>() {

    private var disposable = CompositeDisposable()

    abstract fun loadInitialRequest(params: LoadInitialParams<Int>): Single<FlickrPhotoSearchResponse>
    abstract fun loadBeforeRequest(params: LoadParams<Int>): Single<FlickrPhotoSearchResponse>
    abstract fun loadAfterRequest(params: LoadParams<Int>): Single<FlickrPhotoSearchResponse>

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, FlickrPhoto>) {
        disposable.add(loadInitialRequest(params).subscribe { response, error ->
            if (error == null) {
                callback.onResult(response.photos.photo, 0, response.photos.total, null, 2)
            } else {
                println(error)
                callback.onResult(listOf(), 0, 0, null, null)
            }
        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, FlickrPhoto>) {
        disposable.add(loadAfterRequest(params).subscribe { response, error ->
            if (error == null) {
                callback.onResult(response.photos.photo, response.photos.page + 1)
            } else {
                println(error)
                callback.onResult(listOf(), null)
            }
        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, FlickrPhoto>) {
        disposable.add(loadBeforeRequest(params).subscribe { response, error ->
            if (error == null) {
                callback.onResult(response.photos.photo, response.photos.page - 1)
            } else {
                println(error)
                callback.onResult(listOf(), null)
            }
        })
    }

    override fun invalidate() {
        disposable.dispose()
        super.invalidate()
    }
}