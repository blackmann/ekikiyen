package blackground.ekikiyen.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import blackground.ekikiyen.R
import blackground.ekikiyen.adapters.EkikimeAdapter

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
}