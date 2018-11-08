package vdung.android.quickflick.ui

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import vdung.android.quickflick.R
import vdung.android.quickflick.ui.main.MainFragment

class MainActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.content, MainFragment())
                .commitNow()
        }
    }
}
