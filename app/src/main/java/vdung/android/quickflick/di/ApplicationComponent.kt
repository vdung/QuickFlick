package vdung.android.quickflick.di

import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import vdung.android.quickflick.QuickFlickApplication
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApplicationModule::class,
        ActivityBindingModule::class,
        AndroidSupportInjectionModule::class,
        DataModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<QuickFlickApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<QuickFlickApplication>()
}

@Module
class ApplicationModule {

    @Provides
    fun provideContext(application: QuickFlickApplication): Context {
        return application.applicationContext
    }
}