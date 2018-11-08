package vdung.android.quickflick.ui.common

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class DataBindingViewHolder<T, VDB : ViewDataBinding>(val binding: VDB) : RecyclerView.ViewHolder(binding.root) {
    fun bind(variableId: Int, item: T) {
        binding.setVariable(variableId, item)
    }
}