package vdung.android.quickflick.data.flickr

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FlickrTag(
    val score: Int,
    @Json(name = "_content") val content: String
)

@JsonClass(generateAdapter = true)
data class FlickrHotTagsResponse(
    @Json(name = "hottags") val hotTags: HotTags,
    val stat: String
) {
    @JsonClass(generateAdapter = true)
    data class HotTags(
        val period: String,
        val count: Int,
        val tag: List<FlickrTag>
    )
}