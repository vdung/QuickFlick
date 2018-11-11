package vdung.android.quickflick.data.glide

import com.bumptech.glide.load.Option
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import vdung.android.quickflick.data.flickr.FlickrPhoto
import java.io.InputStream
import javax.inject.Inject
import kotlin.math.max

class FlickrModelLoader(concreteLoader: ModelLoader<GlideUrl, InputStream>) :
    BaseGlideUrlLoader<FlickrPhoto>(concreteLoader) {

    companion object {
        val THUMBNAIL = Option.memory("flickr_thumbnail", false)
    }

    override fun getUrl(model: FlickrPhoto, width: Int, height: Int, options: Options): String? {
        val size = max(width, height)
        val isThumbnail = options.get(THUMBNAIL)!!

        return when {
            size > 1024 && model.largeUrl != null && !isThumbnail -> model.largeUrl
            size > 640 && model.mediumLargeUrl != null && !isThumbnail -> model.mediumLargeUrl
            size > 240 && model.mediumUrl != null -> model.mediumUrl
            size > 100 && model.smallUrl != null -> model.smallUrl
            else -> model.thumbnailUrl
        }
    }

    override fun handles(model: FlickrPhoto): Boolean {
        return true
    }

    class Factory @Inject constructor() : ModelLoaderFactory<FlickrPhoto, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<FlickrPhoto, InputStream> {
            val urlLoader = multiFactory.build(GlideUrl::class.java, InputStream::class.java)
            return FlickrModelLoader(urlLoader)
        }

        override fun teardown() {}
    }
}