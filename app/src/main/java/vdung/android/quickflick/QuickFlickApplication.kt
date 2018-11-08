package vdung.android.quickflick

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import vdung.android.quickflick.di.DaggerApplicationComponent

class QuickFlickApplication : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder().create(this)
    }
}