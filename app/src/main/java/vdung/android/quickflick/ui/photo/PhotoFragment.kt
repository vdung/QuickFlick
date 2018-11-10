package vdung.android.quickflick.ui.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import dagger.android.support.DaggerFragment
import vdung.android.quickflick.databinding.PhotoFragmentBinding
import vdung.android.quickflick.di.GlideApp
import vdung.android.quickflick.ui.common.addStartTransitionListener
import javax.inject.Inject

class PhotoFragment : DaggerFragment() {
    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): PhotoFragment {
            return PhotoFragment().apply {
                arguments = Bundle().also {
                    it.putInt(ARG_POSITION, position)
                }
            }
        }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: PhotoViewModel
    private lateinit var binding: PhotoFragmentBinding

    internal val sharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
            val key = names[0]
            sharedElements[key] = binding.photoView
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PhotoFragmentBinding.inflate(inflater, container, false).also {
            it.setLifecycleOwner(this)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get()
        val position = arguments!!.getInt(ARG_POSITION)
        val photo = viewModel.interestingPhotos.value?.value?.get(position)

        photo?.let { ViewCompat.setTransitionName(binding.photoView, it.id) }

        GlideApp.with(this)
            .load(photo)
            .addStartTransitionListener(requireActivity())
            .into(binding.photoView)
    }
}