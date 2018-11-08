package vdung.android.quickflick.data.flickr

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class FlickrExtras(val key: String) {
    Small320(FlickrExtras.SMALL_320),
    Medium640(FlickrExtras.MEDIUM_640),
    Medium800(FlickrExtras.MEDIUM_800),
    Large(FlickrExtras.LARGE),
    Original(FlickrExtras.ORIGINAL),
    OriginalDimension(FlickrExtras.ORIGINAL_DIMENSION);

    companion object {
        const val SMALL_320 = "url_n"
        const val MEDIUM_640 = "url_z"
        const val MEDIUM_800 = "url_c"
        const val LARGE = "url_b"
        const val ORIGINAL = "url_o"
        const val ORIGINAL_DIMENSION = "o_dims"
        const val SMALL_320_WIDTH = "width_n"
        const val SMALL_320_HEIGHT = "height_n"

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
    @Json(name = FlickrExtras.SMALL_320_WIDTH) val smallWidth: Int,
    @Json(name = FlickrExtras.SMALL_320_HEIGHT) val smallHeight: Int,
    @Json(name = FlickrExtras.SMALL_320) val smallUrl: String,
    @Json(name = FlickrExtras.MEDIUM_640) val mediumUrl: String,
    @Json(name = FlickrExtras.ORIGINAL) val originalUrl: String?
)

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