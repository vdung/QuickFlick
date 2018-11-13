package vdung.android.quickflick.data.flickr

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class FlickrExtras(val key: String) {
    Thumbnail(FlickrExtras.THUMBNAIL),
    Small(FlickrExtras.SMALL),
    Medium640(FlickrExtras.MEDIUM_640),
    Large1024(FlickrExtras.LARGE_1024),
    Large1600(FlickrExtras.LARGE_1600),
    Original(FlickrExtras.ORIGINAL),
    Tags(FlickrExtras.TAGS);

    companion object {
        const val THUMBNAIL = "url_t"
        const val SMALL = "url_m"
        const val MEDIUM_640 = "url_z"
        const val LARGE_1024 = "url_b"
        const val LARGE_1600 = "url_h"
        const val ORIGINAL = "url_o"
        const val THUMBNAIL_WIDTH = "width_t"
        const val THUMBNAIL_HEIGHT = "height_t"
        const val TAGS = "tags"

        fun allValues(): String {
            return FlickrExtras.values().joinToString(separator = ",") { it.key }
        }
    }
}

@JsonClass(generateAdapter = true)
data class FlickrPhoto(
    val id: String,
    val title: String,
    val owner: String,
    val secret: String,
    val farm: String,
    val server: String,
    val tags: String,
    @Json(name = FlickrExtras.THUMBNAIL_WIDTH) val thumbnailWidth: Int,
    @Json(name = FlickrExtras.THUMBNAIL_HEIGHT) val thumbnailHeight: Int,
    @Json(name = FlickrExtras.THUMBNAIL) val thumbnailUrl: String?,
    @Json(name = FlickrExtras.SMALL) val smallUrl: String?,
    @Json(name = FlickrExtras.MEDIUM_640) val mediumUrl: String?,
    @Json(name = FlickrExtras.LARGE_1024) val mediumLargeUrl: String?,
    @Json(name = FlickrExtras.LARGE_1600) val largeUrl: String?,
    @Json(name = FlickrExtras.ORIGINAL) val originalUrl: String?
) {
    val webUrl: String get() {
        return "https://www.flickr.com/photos/$owner/$id/"
    }
}

@JsonClass(generateAdapter = true)
data class FlickrPhotoSearchResponse(
    val photos: Photos,
    val stat: String
) {
    @JsonClass(generateAdapter = true)
    data class Photos(
        val page: Int,
        val pages: Int,
        @Json(name = "perpage") val perPage: Int,
        val total: Int,
        val photo: List<FlickrPhoto>
    )
}