package vdung.android.quickflick.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import vdung.android.quickflick.BR

abstract class DataBindingPagedListAdapter<Item, VDB : ViewDataBinding>(diffUtilCallback: DiffUtil.ItemCallback<Item>) :
    PagedListAdapter<Item, DataBindingViewHolder<Item, VDB>>(diffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder<Item, VDB> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<VDB>(inflater, viewType, parent, false)
        return DataBindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder<Item, VDB>, position: Int) {
        getItem(position)?.let { holder.bind(getVariableId(), it) }
    }

    override fun onViewRecycled(holder: DataBindingViewHolder<Item, VDB>) {
        super.onViewRecycled(holder)
        holder.binding.unbind()
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutId(position)
    }

    open fun getVariableId(): Int {
        return BR.item
    }

    abstract fun getLayoutId(position: Int): Int
}