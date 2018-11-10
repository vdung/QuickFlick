package vdung.android.quickflick.ui.common

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import vdung.android.quickflick.di.GlideRequest

object RecyclerViewTransitions {

    interface ElementProvider {
        fun getSharedElements(names: List<String>): Map<String, View>
    }

    fun to(elementProvider: ElementProvider): SharedElementCallback {
        return object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                sharedElements.putAll(elementProvider.getSharedElements(names))
            }
        }
    }

    fun to(
        recyclerView: RecyclerView,
        getPosition: () -> Int,
        getViews: (RecyclerView.ViewHolder, List<String>) -> Map<String, View>
    ): SharedElementCallback {
        return to(recyclerView.asElementProvider(getPosition, getViews))
    }
}

fun RecyclerView.asElementProvider(
    getPosition: () -> Int,
    getViews: (RecyclerView.ViewHolder, List<String>) -> Map<String, View>
): RecyclerViewTransitions.ElementProvider {
    return object : RecyclerViewTransitions.ElementProvider {
        override fun getSharedElements(names: List<String>): Map<String, View> {
            val viewHolder = findViewHolderForAdapterPosition(getPosition()) ?: return emptyMap()
            return getViews(viewHolder, names)
        }
    }
}

fun RecyclerView.executePostponedTransition(activity: FragmentActivity, position: Int) {
    layoutManager?.let {
        val viewAtPosition = it.findViewByPosition(position)
        if (viewAtPosition == null || it.isViewPartiallyVisible(viewAtPosition, false, true)) {
            scrollToPosition(position)
        }
    }

    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            viewTreeObserver.removeOnPreDrawListener(this)
            activity.supportStartPostponedEnterTransition()
            return false
        }
    })
}

fun RecyclerView.attachExitSharedElementCallback(
    activity: FragmentActivity,
    getPosition: () -> Int,
    getViews: (RecyclerView.ViewHolder, List<String>) -> Map<String, View>
) {
    activity.setExitSharedElementCallback(RecyclerViewTransitions.to(this, getPosition, getViews))
}

fun <T> GlideRequest<T>.addStartTransitionListener(activity: FragmentActivity) = apply {
    addListener(object : RequestListener<T> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<T>?,
            isFirstResource: Boolean
        ): Boolean {
            activity.supportStartPostponedEnterTransition()
            return false
        }

        override fun onResourceReady(
            resource: T,
            model: Any?,
            target: Target<T>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            activity.supportStartPostponedEnterTransition()
            return false
        }

    })
}