package vdung.android.quickflick.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import androidx.paging.PagedList
import vdung.android.quickflick.data.flickr.FlickrPhoto
import vdung.android.quickflick.data.flickr.FlickrPhotoSearchResponse
import vdung.android.quickflick.data.flickr.FlickrRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val flickrRepository: FlickrRepository
) : ViewModel() {
    fun searchPhotos(): LiveData<PagedList<FlickrPhoto>> = flickrRepository.getInterestingPhotos().toLiveData()
}