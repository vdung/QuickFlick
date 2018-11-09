package vdung.android.quickflick.di

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import vdung.android.quickflick.R
import vdung.android.quickflick.data.flickr.FlickrRepository
import vdung.android.quickflick.data.flickr.FlickrService
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    fun provideCallFactory(context: Context): Call.Factory {
        return OkHttpClient.Builder()
            .addInterceptor {
                it.proceed(
                    it.request().run {
                        val url = url()
                            .newBuilder()
                            .addQueryParameter("api_key", context.getString(R.string.api_key))
                            .addQueryParameter("format", "json")
                            .addQueryParameter("nojsoncallback", "1")
                            .build()

                        println(url)

                        return@run newBuilder()
                            .url(url)
                            .build()
                    }
                )
            }
            .build()
    }

    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    fun provideRetrofit(callFactory: Call.Factory, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .callFactory(callFactory)
            .baseUrl(" https://api.flickr.com/services/rest/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    fun provideFlickrService(retrofit: Retrofit): FlickrService {
        return retrofit.create(FlickrService::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(service: FlickrService): FlickrRepository {
        return FlickrRepository(service)
    }
}
