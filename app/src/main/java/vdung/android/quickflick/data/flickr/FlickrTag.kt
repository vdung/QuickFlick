package vdung.android.quickflick.data.flickr

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FlickrTag(
    @Json(name = "_content") val content: String,
    val score: Int? = null
)

@JsonClass(generateAdapter = true)
data class FlickrRelatedTagsResponse(
    val tags: Tags
) {
    @JsonClass(generateAdapter = true)
    data class Tags(
        val source: String,
        val tag: List<FlickrTag>
    )
}