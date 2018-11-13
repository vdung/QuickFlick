package vdung.android.quickflick.ui.photo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import dagger.android.support.DaggerFragment
import vdung.android.quickflick.R
import vdung.android.quickflick.data.glide.FlickrModelLoader
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        val photo = viewModel.getPhoto(position)

        binding.photoView.setOnClickListener(photoClickListener)

        photo?.let {
            ViewCompat.setTransitionName(binding.photoView, it.id)

            GlideApp.with(this)
                .load(it)
                .thumbnail(
                    GlideApp.with(this)
                        .load(it)
                        .set(FlickrModelLoader.THUMBNAIL, true)
                        .onlyRetrieveFromCache(true)
                        .addStartTransitionListener(requireActivity())
                )
                .dontTransform()
                .addStartTransitionListener(requireActivity())
                .into(binding.photoView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.photo_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val photo = viewModel.getPhoto(position) ?: return false
        return when (item.itemId) {
            R.id.open_in_browser -> {
                startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_VIEW, Uri.parse(photo.webUrl)),
                        getString(R.string.action_open_photo)
                    )
                )
                true
            }
            else -> false
        }
    }

    private val position: Int
        get() {
            return arguments!!.getInt(ARG_POSITION)
        }

    private val photoClickListener = View.OnClickListener {
        val actionBar = (requireActivity() as? AppCompatActivity)?.supportActionBar ?: return@OnClickListener
        if (actionBar.isShowing) {
            actionBar.hide()
        } else {
            actionBar.show()
        }
    }
}