package blackground.ekikiyen.home

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import blackground.ekikiyen.R
import blackground.ekikiyen.about.view.AboutActivity
import blackground.ekikiyen.databinding.ViewDialerBinding
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class HomeActivity : AppCompatActivity() {
    private var publisher: View? = null
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_home)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_scan)

        viewPager = findViewById(R.id.fragment_container)
        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    1 -> MainFragment.get()
                    0 -> ScannerFragment.get()

                    else -> MainFragment.get()
                }
            }

            override fun getCount(): Int = 2

        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    1 -> {
                        setMainIcon()
                        findViewById<FloatingActionButton>(R.id.dial)
                                .show()
                    }
                    0 -> {
                        setScannerIcon()
                        findViewById<FloatingActionButton>(R.id.dial)
                                .hide()
                    }
                }

                getSharedPreferences("pages", Context.MODE_PRIVATE)
                        .edit()
                        .putInt("selected_page", position)
                        .apply()
            }

        })

        val lastPage = getSharedPreferences("pages", Context.MODE_PRIVATE)
                .getInt("selected_page", 1)

        viewPager.setCurrentItem(lastPage, true)

        // icon not changing tint when the selected is the scanner
        if (lastPage == 0) {
            setScannerIcon()
            findViewById<FloatingActionButton>(R.id.dial)
                    .hide()
        }

        findViewById<FloatingActionButton>(R.id.dial)
                .setOnClickListener { showDialer() }
    }

    private fun showDialer() {
        val inflater = LayoutInflater.from(this)

        val dialerBinding: ViewDialerBinding =
                DataBindingUtil.inflate(inflater, R.layout.view_dialer, null, false)

        val bottomSheetDialer = BottomSheetDialog(this)
        bottomSheetDialer.setContentView(dialerBinding.root)
        bottomSheetDialer.show()

        val viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        dialerBinding.viewModel = viewModel
        dialerBinding.delete.setOnLongClickListener { viewModel.clearDialer() }

        publisher = dialerBinding.publisher
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.home, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> refresh()
            R.id.about -> goToAbout()
            R.id.recommend -> recommendApp()
            android.R.id.home -> switchPages()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun switchPages() {
        // we are switching, meaning if we are on the second page
        // we change to the first page, and vice versa
        when (viewPager.currentItem) {
            1 -> switchToScanner()
            0 -> switchToMain()
        }
    }

    fun switchToMain() {
        viewPager.setCurrentItem(1, true)
        setMainIcon()
    }

    private fun switchToScanner() {
        viewPager.setCurrentItem(0, true)
        setScannerIcon()
    }

    @Suppress("DEPRECATION")
    private fun setMainIcon() {
        val scannerIcon = resources.getDrawable(R.drawable.ic_scan)
        scannerIcon.setColorFilter(resources.getColor(R.color.darker_gray), PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(scannerIcon)
    }

    @Suppress("DEPRECATION")
    private fun setScannerIcon() {
        val scannerIcon = resources.getDrawable(R.drawable.ic_scan)
        scannerIcon.setColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)

        supportActionBar?.setHomeAsUpIndicator(scannerIcon)
    }

    private fun recommendApp() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Ekiki Yen is here.\nSpend your credit on internet bundle and use Ekiki Me for " +
                "call. Get the idea!\nFind Ekiki me codes with this app. You can also share your card after reloading. All so simple. \n" +
                "Download from here: https://play.google.com/store/apps/details?id=blackground.ekikiyen")
        shareIntent.type = "text/plain"
        startActivity(shareIntent)
    }

    private fun refresh() {
        ViewModelProviders.of(this)
                .get(HomeViewModel::class.java)
                .getAll()
    }

    private fun goToAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }
}