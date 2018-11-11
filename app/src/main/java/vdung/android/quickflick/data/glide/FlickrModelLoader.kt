package vdung.android.quickflick.data.glide

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
    override fun getUrl(model: FlickrPhoto, width: Int, height: Int, options: Options): String? {
        val size = max(width, height) * 0.75
        println(size)
        return when {
            size > 1600 && model.largeUrl != null -> model.largeUrl
            size > 1024 && model.mediumLargeUrl != null -> model.mediumLargeUrl
            size > 640 && model.originalUrl != null -> model.originalUrl
            size > 240 && model.mediumUrl != null -> model.mediumUrl
            model.smallUrl != null -> model.smallUrl
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