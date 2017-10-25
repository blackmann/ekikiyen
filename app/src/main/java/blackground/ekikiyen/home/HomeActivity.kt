package blackground.ekikiyen.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import blackground.ekikiyen.R
import blackground.ekikiyen.about.view.AboutActivity
import blackground.ekikiyen.adapters.EkikimeAdapter
import blackground.ekikiyen.data.Ekikime
import blackground.ekikiyen.databinding.ViewDialerBinding
import blackground.ekikiyen.databinding.ViewUsedBinding
import java.util.*
import kotlin.collections.HashSet

class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: EkikimeAdapter
    private var selectedCard = ""

    private var publisher: View? = null

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

        if (card.length < 14) {
            Toast.makeText(this, "Recharge code is incomplete.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!publishTipShown()) {
            Log.i("Ekiki", "Tooltip showing $publisher")
            AlertDialog.Builder(this)
                    .setView(R.layout.publish_dialog)
                    .setPositiveButton("Continue to load credit") { dialog, _ ->
                        run {
                            dial(card)
                            dialog.dismiss()
                            getSharedPreferences("ek_tip", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("ek_tip_shown", true)
                                    .apply()
                        }
                    }
                    .create()
                    .show()

        } else {
            dial(card)
        }
    }

    private fun dial(card: String?) {
        if (card == null) {
            return
        }

        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*134*$card%23"))
        startActivity(dialIntent)
    }

    private fun publishTipShown(): Boolean {
        return getSharedPreferences("ek_tip", Context.MODE_PRIVATE)
                .getBoolean("ek_tip_shown", false)
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

        // pre-process
        val usedCardsToday = getUsedCards()
        if (usedCardsToday.size > 1) {
            Toast.makeText(this, "You cannot load Ekiki me twice a day. Please come back tomorrow.",
                    Toast.LENGTH_LONG).show()
            return
        }

        if (usedCardsToday.contains(card)) {
            Toast.makeText(this, "You have already used this card, try another",
                    Toast.LENGTH_LONG).show()
            return
        }

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

            saveToPrefs(card)
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

        publisher = dialerBinding.publisher
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

    private fun saveToPrefs(card: String) {
        val sharedPreferences = getSharedPreferences(Date().getDateString(), Context.MODE_PRIVATE)
        val cards = sharedPreferences.getStringSet("today", HashSet<String>()) as HashSet<String>

        cards.add(card)

        sharedPreferences.edit()
                .putStringSet("today", cards)
                .apply()
    }

    private fun getUsedCards(): HashSet<String> {
        val sharedPreferences = getSharedPreferences(Date().getDateString(), Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("today", HashSet<String>()) as HashSet<String>
    }

    // util
    private fun Date.getDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.time = this

        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val date = calendar.get(Calendar.DAY_OF_MONTH)

        return "$date-$month-$year"
    }
}