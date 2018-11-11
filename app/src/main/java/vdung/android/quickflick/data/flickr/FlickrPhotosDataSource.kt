package vdung.android.quickflick.data.flickr

import androidx.paging.PageKeyedDataSource
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

abstract class FlickrPhotosDataSource : PageKeyedDataSource<Int, FlickrPhoto>() {

    private var disposable = CompositeDisposable()

    abstract fun loadInitialRequest(params: LoadInitialParams<Int>): Single<FlickrPhotoSearchResponse>
    abstract fun loadBeforeRequest(params: LoadParams<Int>): Single<FlickrPhotoSearchResponse>
    abstract fun loadAfterRequest(params: LoadParams<Int>): Single<FlickrPhotoSearchResponse>

    open fun onRetry(retryCount: Int, error: Throwable): Boolean {
        return false
    }

    open fun onError(error: Throwable) {
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, FlickrPhoto>) {
        disposable.add(
            Flowable.defer { loadInitialRequest(params).toFlowable() }
                .retry(this::onRetry)
                .subscribe(
                    { response -> callback.onResult(response.photos.photo, 0, response.photos.total, null, 2) },
                    this::onError
                )
        )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, FlickrPhoto>) {
        disposable.add(Flowable.defer { loadAfterRequest(params).toFlowable() }
            .retry(this::onRetry)
            .subscribe(
                { response -> callback.onResult(response.photos.photo, response.photos.page + 1) },
                this::onError
            ))
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, FlickrPhoto>) {
        disposable.add(Flowable.defer { loadBeforeRequest(params).toFlowable() }
            .retry(this::onRetry)
            .subscribe(
                { response -> callback.onResult(response.photos.photo, response.photos.page - 1) },
                this::onError
            ))
    }

    override fun invalidate() {
        disposable.dispose()
        super.invalidate()
    }
}