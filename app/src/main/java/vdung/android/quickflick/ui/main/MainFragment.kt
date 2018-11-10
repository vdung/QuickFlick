package vdung.android.quickflick.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.android.support.DaggerFragment
import vdung.android.quickflick.R
import vdung.android.quickflick.data.flickr.FlickrPhoto
import vdung.android.quickflick.databinding.MainFragmentBinding
import vdung.android.quickflick.databinding.MainRecyclerViewItemBinding
import vdung.android.quickflick.di.GlideApp
import vdung.android.quickflick.ui.common.*
import vdung.android.quickflick.ui.photo.PhotoActivity
import javax.inject.Inject

class MainFragment : DaggerFragment(), OnActivityReenterListener {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel

    var currentPosition = 0
    private var hasPendingTransition = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (requireActivity() as? OnActivityReenterListener.Host)?.addListener(this)
    }

    override fun onDetach() {
        (requireActivity() as? OnActivityReenterListener.Host)?.removeListener(this)
        super.onDetach()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MainFragmentBinding.inflate(inflater, container, false).also {
            it.setLifecycleOwner(this)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get()
        val adapter = Adapter()

        binding.viewModel = viewModel
        binding.apply {
            recyclerView.apply {
                this.adapter = adapter
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                addItemDecoration(
                    StaggeredGridInsetDecoration(
                        resources.getDimensionPixelSize(R.dimen.grid_edge_inset),
                        resources.getDimensionPixelSize(R.dimen.grid_inner_inset)
                    )
                )

                attachExitSharedElementCallback(requireActivity(), {
                    currentPosition
                }) { viewHolder, names ->
                    val binding = viewHolder.let {
                        @Suppress("UNCHECKED_CAST")
                        it as DataBindingViewHolder<FlickrPhoto, MainRecyclerViewItemBinding>
                    }.binding

                    return@attachExitSharedElementCallback mapOf(names[0] to binding.imageView)
                }

                if (hasPendingTransition) {
                    executePostponedTransition(requireActivity(), currentPosition)
                }
            }
        }

        viewModel.searchPhotos().observe(this, Observer {
            adapter.submitList(it.value)
        })
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        val position = data?.getIntExtra(PhotoActivity.ARG_CURRENT_POSITION, 0) ?: return
        requireActivity().supportPostponeEnterTransition()

        currentPosition = position
        if (::binding.isInitialized) {
            binding.recyclerView.executePostponedTransition(requireActivity(), currentPosition)
        } else {
            hasPendingTransition = true
        }
    }

    val onItemClickListener = View.OnClickListener { v ->
        val holder = binding.recyclerView.findContainingViewHolder(v)?.let {
            @Suppress("UNCHECKED_CAST")
            it as? DataBindingViewHolder<FlickrPhoto, MainRecyclerViewItemBinding>
        } ?: return@OnClickListener

        currentPosition = holder.adapterPosition

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            Pair(holder.binding.imageView, holder.binding.imageView.transitionName)
        )
        startActivity(
            PhotoActivity.launchIntent(requireActivity(), currentPosition),
            options.toBundle()
        )
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<FlickrPhoto>() {
            override fun areItemsTheSame(oldItem: FlickrPhoto, newItem: FlickrPhoto): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FlickrPhoto, newItem: FlickrPhoto): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class Adapter : DataBindingPagedListAdapter<FlickrPhoto, MainRecyclerViewItemBinding>(diffCallback) {
        override fun onBindViewHolder(
            holder: DataBindingViewHolder<FlickrPhoto, MainRecyclerViewItemBinding>,
            position: Int
        ) {
            super.onBindViewHolder(holder, position)

            getItem(position)?.let { it ->
                holder.binding.apply {
                    ConstraintSet().apply {
                        clone(constraintView)
                        setDimensionRatio(R.id.image_view, "${it.smallWidth}:${it.smallHeight}")
                        applyTo(constraintView)
                    }
                    ViewCompat.setTransitionName(imageView, it.id)

                    GlideApp.with(holder.itemView)
                        .load(it)
                        .into(holder.binding.imageView)

                    root.setOnClickListener(onItemClickListener)
                }
            }
        }

        override fun getLayoutId(position: Int): Int {
            return R.layout.main_recycler_view_item
        }
    }
}