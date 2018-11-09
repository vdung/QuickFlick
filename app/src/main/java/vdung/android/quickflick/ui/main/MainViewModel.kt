package vdung.android.quickflick.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import androidx.paging.PagedList
import io.reactivex.Flowable
import vdung.android.quickflick.data.Result
import vdung.android.quickflick.data.fetch
import vdung.android.quickflick.data.flickr.FlickrPhoto
import vdung.android.quickflick.data.flickr.FlickrRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val flickrRepository: FlickrRepository
) : ViewModel() {

    private val interestingPhotos = flickrRepository.interestingPhotos

    fun searchPhotos(): LiveData<Result<PagedList<FlickrPhoto>>> = interestingPhotos.toLiveData()
    val isRefreshing = Flowable.fromPublisher(interestingPhotos).map { it is Result.Pending }.toLiveData()

    fun refresh() {
        interestingPhotos.fetch()
    }
}