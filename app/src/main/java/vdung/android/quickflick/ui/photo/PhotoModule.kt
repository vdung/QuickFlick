package vdung.android.quickflick.ui.photo

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import vdung.android.quickflick.di.FragmentScoped
import vdung.android.quickflick.di.ViewModelKey

@Module
abstract class PhotoModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun photoFragment(): PhotoFragment

    @Binds
    @IntoMap
    @ViewModelKey(PhotoViewModel::class)
    abstract fun bindViewModel(viewModel: PhotoViewModel): ViewModel
}