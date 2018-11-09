package vdung.android.quickflick.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
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
import vdung.android.quickflick.ui.common.DataBindingPagedListAdapter
import vdung.android.quickflick.ui.common.DataBindingViewHolder
import vdung.android.quickflick.ui.common.StaggeredGridInsetDecoration
import javax.inject.Inject

class MainFragment : DaggerFragment() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel

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
            recyclerView.let {
                it.adapter = adapter
                it.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                it.addItemDecoration(
                    StaggeredGridInsetDecoration(
                        resources.getDimensionPixelSize(R.dimen.grid_edge_inset),
                        resources.getDimensionPixelSize(R.dimen.grid_inner_inset)
                    )
                )
            }
        }

        viewModel.searchPhotos().observe(this, Observer {
            adapter.submitList(it.value)
        })
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

                    GlideApp.with(holder.itemView)
                        .load(it)
                        .into(holder.binding.imageView)
                }
            }
        }

        override fun getLayoutId(position: Int): Int {
            return R.layout.main_recycler_view_item
        }
    }
}