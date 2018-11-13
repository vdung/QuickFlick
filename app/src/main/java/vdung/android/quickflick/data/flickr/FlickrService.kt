package vdung.android.quickflick.data.flickr

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrService {

    @GET("?method=flickr.photos.search")
    fun searchPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("text") text: String,
        @Query("tags") tags: String,
        @Query("tag_mode") tagMode: String = "all",
        @Query("sort") sortBy: String = "interestingness-desc",
        @Query("extras") extras: String
    ): Single<FlickrPhotoSearchResponse>

    @GET("?method=flickr.interestingness.getList")
    fun getInterestingPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("extras") extras: String
    ): Single<FlickrPhotoSearchResponse>

    @GET("?method=flickr.tags.getRelated")
    fun getRelatedTags(
        @Query("tag") tag: String
    ): Single<FlickrRelatedTagsResponse>
}