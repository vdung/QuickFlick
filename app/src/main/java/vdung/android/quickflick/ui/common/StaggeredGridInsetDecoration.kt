package vdung.android.quickflick.ui.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class StaggeredGridInsetDecoration(
    private val edgeInset: Int,
    private val innerInset: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val layoutManager = parent.layoutManager as StaggeredGridLayoutManager
        val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

        val spanCount = layoutManager.spanCount
        val spanIndex = layoutParams.spanIndex

        outRect.left = if (spanIndex == 0) edgeInset else innerInset / 2
        outRect.right = if (spanIndex == spanCount - 1) edgeInset else innerInset / 2
        outRect.top = if (position < spanCount) edgeInset else innerInset / 2
        outRect.bottom = innerInset / 2
    }
}