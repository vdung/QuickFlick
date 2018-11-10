package vdung.android.quickflick.ui.common

import android.content.Intent

interface OnActivityReenterListener {
    fun onActivityReenter(resultCode: Int, data: Intent?)

    interface Host {
        fun addListener(listener: OnActivityReenterListener)

        fun removeListener(listener: OnActivityReenterListener)
    }

    class HostDelegate : Host, OnActivityReenterListener {

        private val listeners = mutableListOf<OnActivityReenterListener>()

        override fun onActivityReenter(resultCode: Int, data: Intent?) {
            for (listener in listeners) {
                listener.onActivityReenter(resultCode, data)
            }
        }

        override fun addListener(listener: OnActivityReenterListener) {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }

        override fun removeListener(listener: OnActivityReenterListener) {
            listeners.remove(listener)
        }

    }
}