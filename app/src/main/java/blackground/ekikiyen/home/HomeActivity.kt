package blackground.ekikiyen.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import blackground.ekikiyen.R
import blackground.ekikiyen.about.view.AboutActivity
import blackground.ekikiyen.adapters.EkikimeAdapter
import blackground.ekikiyen.data.Ekikime
import blackground.ekikiyen.databinding.ViewDialerBinding
import blackground.ekikiyen.databinding.ViewUsedBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: EkikimeAdapter
    private var selectedCard = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<FloatingActionButton>(R.id.dial)
                .setOnClickListener { showDialer() }

        val viewModel = ViewModelProviders.of(this)
                .get(HomeViewModel::class.java)

        val ekikimeList = findViewById<RecyclerView>(R.id.list)
        adapter = EkikimeAdapter(onCardClick = object : EkikimeAdapter.IOnCardClick {
            override fun onClick(cardNumber: String) {
                ekikime(cardNumber)
            }
        })
        ekikimeList.adapter = adapter

        viewModel.ekikimeList
                .observe(this, Observer { updateList(it) })

        viewModel.dialNumber
                .observe(this, Observer { loadCard(it) })

        viewModel.hideLoading
                .observe(this, Observer { hideLoading() })

        viewModel.showLoading
                .observe(this, Observer { showLoading() })

        viewModel.requestRefresh
                .observe(this, Observer { viewModel.getAll() })

        // now let the view model fetch the items
        viewModel.getAll()
    }

    private fun updateList(list: ArrayList<Ekikime>?) {
        if (list == null) return

        if (list.isEmpty()) {
            showEmpty()
        } else {
            hideEmpty()
        }

        adapter.update(list)
    }

    private fun showEmpty() {
        findViewById<LinearLayout>(R.id.empty)
                .visibility = View.VISIBLE
    }

    private fun hideEmpty() {
        findViewById<LinearLayout>(R.id.empty)
                .visibility = View.GONE
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

    private fun showLoading() {
        findViewById<LinearLayout>(R.id.loading).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<LinearLayout>(R.id.loading).visibility = View.GONE
    }

    private fun loadCard(card: String?) {
        if (card == null) {
            return
        }

        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*134*$card%23"))
        startActivity(dialIntent)
    }

    private fun refresh() {
        ViewModelProviders.of(this)
                .get(HomeViewModel::class.java)
                .getAll()
    }

    private fun goToAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun ekikime(card: String) {
        selectedCard = card

        // show confirmation dialog
        val confirmDialog = BottomSheetDialog(this)
        val contentBinding: ViewUsedBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.view_used, null, false)

        confirmDialog.setContentView(contentBinding.root)
        confirmDialog.setCancelable(false)
        confirmDialog.show()

        val viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        contentBinding.yes.setOnClickListener {
            viewModel.use(card)
            confirmDialog.dismiss()
        }

        contentBinding.no.setOnClickListener {
            confirmDialog.dismiss()
        }

        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*135*$card%23"))
        startActivity(dialIntent)
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
    }

    private fun recommendApp() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Ekiki Yen is here.\nSpend your credit on internet bundle and use Ekiki Me for " +
                "call. Get the idea!\nFind Ekiki me codes with this app. You can also share your card after reloading. All so simple. \n" +
                "Download from here :")
        shareIntent.type = "text/plain"
        startActivity(shareIntent)
    }
}