package blackground.ekikiyen.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import blackground.ekikiyen.R
import blackground.ekikiyen.adapters.EkikimeAdapter
import blackground.ekikiyen.databinding.ViewDialerBinding

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<FloatingActionButton>(R.id.dial)
                .setOnClickListener { showDialer() }

        val viewModel = ViewModelProviders.of(this)
                .get(HomeViewModel::class.java)

        val ekikimeList = findViewById<RecyclerView>(R.id.list)
        val adapter = EkikimeAdapter()
        ekikimeList.adapter = adapter

        viewModel.ekikimeList
                .observe(this, Observer { adapter.update(it) })

        // now let the view model fetch the items
        viewModel.getAll()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.home, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> refresh()
            R.id.about -> goToAbout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refresh() {

    }

    private fun goToAbout() {

    }

    private fun showDialer() {
        val inflater = LayoutInflater.from(this)

        val dialerBinding: ViewDialerBinding =
                DataBindingUtil.inflate(inflater, R.layout.view_dialer, null, false)

        val bottomSheetDialer = BottomSheetDialog(this)
        bottomSheetDialer.setContentView(dialerBinding.root)
        bottomSheetDialer.show()

        dialerBinding.viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }
}