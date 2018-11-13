package vdung.android.quickflick.ui.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import vdung.android.quickflick.data.flickr.FlickrPhoto
import vdung.android.quickflick.data.flickr.FlickrRepository
import javax.inject.Inject

class PhotoViewModel @Inject constructor(
    private val flickrRepository: FlickrRepository
) : ViewModel() {
    val interestingPhotos = flickrRepository.interestingPhotos.toLiveData()

    val photoCount: Int get() = interestingPhotos.value?.value?.size ?: 0

    fun getPhoto(position: Int): FlickrPhoto? {
        return interestingPhotos.value?.value?.get(position)
    }
}