package vdung.android.quickflick.ui.photo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.paging.PagedList
import androidx.viewpager.widget.ViewPager
import dagger.android.support.DaggerAppCompatActivity
import vdung.android.quickflick.R
import vdung.android.quickflick.data.Result
import vdung.android.quickflick.databinding.PhotoActivityBinding
import javax.inject.Inject

class PhotoActivity : DaggerAppCompatActivity() {
    companion object {
        private const val ARG_INITIAL_POSITION = "initialPosition"
        const val ARG_CURRENT_POSITION = "currentPosition"

        fun launchIntent(context: Context, initialPosition: Int): Intent {
            return Intent(context, PhotoActivity::class.java).putExtra(ARG_INITIAL_POSITION, initialPosition)
        }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: PhotoViewModel
    private lateinit var binding: PhotoActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportPostponeEnterTransition()

        viewModel = ViewModelProviders.of(this, viewModelFactory).get()
        binding = DataBindingUtil.setContentView(this, R.layout.photo_activity)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var initialPosition = intent.getIntExtra(ARG_INITIAL_POSITION, 0)
        if (savedInstanceState != null) {
            initialPosition = savedInstanceState.getInt(ARG_CURRENT_POSITION, initialPosition)
        }

        viewModel.interestingPhotos.observe(this, Observer { result ->
            if (result !is Result.Success) {
                return@Observer
            }

            binding.pager.apply {
                val adapter = Adapter().also {
                    result.result.addWeakCallback(null, it.callback)
                }
                this.adapter = adapter

                addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        Intent().let {
                            it.putExtra(ARG_CURRENT_POSITION, position)
                            setResult(Activity.RESULT_OK, it)
                        }

                        result.result[position]?.let {
                            title = it.title
                        }

                        adapter.instantiateItem(binding.pager, position)
                            .let { it as PhotoFragment }
                            .let {
                                setEnterSharedElementCallback(it.sharedElementCallback)
                            }
                    }
                })

                currentItem = initialPosition
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_CURRENT_POSITION, binding.pager.currentItem)
    }

    override fun onSupportNavigateUp(): Boolean {
        ActivityCompat.finishAfterTransition(this)
        return true
    }

    private inner class Adapter : FragmentStatePagerAdapter(supportFragmentManager) {

        override fun getItem(position: Int): Fragment {
            return PhotoFragment.newInstance(position)
        }

        override fun getCount(): Int {
            return viewModel.photoCount
        }

        val callback: PagedList.Callback = PagerCallback()

        private inner class PagerCallback : PagedList.Callback() {
            override fun onChanged(position: Int, count: Int) = notifyDataSetChanged()

            override fun onInserted(position: Int, count: Int) = notifyDataSetChanged()

            override fun onRemoved(position: Int, count: Int) = notifyDataSetChanged()
        }
    }
}
