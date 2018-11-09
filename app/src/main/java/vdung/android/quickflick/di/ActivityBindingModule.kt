package vdung.android.quickflick.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import vdung.android.quickflick.ui.main.MainActivity
import vdung.android.quickflick.ui.main.MainModule


@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            MainModule::class
        ]
    )
    internal abstract fun mainActivity(): MainActivity
}