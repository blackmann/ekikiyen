package blackground.ekikiyen.home

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_home)

        var mainFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (mainFragment == null) {
            mainFragment = MainFragment.get()
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, mainFragment)
                    .commit()
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun recommendApp() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Ekiki Yen is here.\nSpend your credit on internet bundle and use Ekiki Me for " +
                "call. Get the idea!\nFind Ekiki me codes with this app. You can also share your card after reloading. All so simple. \n" +
                "Download from here :https://play.google.com/store/apps/details?id=blackground.ekikiyen")
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