package blackground.ekikiyen.home

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import blackground.ekikiyen.R
import blackground.ekikiyen.adapters.EkikimeAdapter
import blackground.ekikiyen.data.Ekikime
import blackground.ekikiyen.databinding.ViewUsedBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class MainFragment : Fragment() {

    private lateinit var adapter: EkikimeAdapter
    private var selectedCard = ""


    // views
    private lateinit var list: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var loading: LinearLayout

    companion object {
        private val RC_CALL_PERM = 121

        fun get(): MainFragment {
            return MainFragment()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list = view.findViewById(R.id.list)
        emptyView = view.findViewById(R.id.empty)
        loading = view.findViewById(R.id.loading)

        val viewModel = ViewModelProviders.of(activity)
                .get(HomeViewModel::class.java)

        adapter = EkikimeAdapter(onCardClick = object : EkikimeAdapter.IOnCardClick {
            override fun onClick(cardNumber: String) {
                ekikime(cardNumber)
            }
        })

        list.layoutManager = LinearLayoutManager(context)
        list.adapter = adapter

        viewModel.ekikimeList
                .observe(this, Observer { updateList(it) })

        viewModel.dialNumber
                .observe(this, Observer { loadCard(it) })

        viewModel.hideLoading
                .observe(this, Observer { hideLoading() })

        viewModel.showLoading
                .observe(this, Observer { showLoading() })

        viewModel.requestRefresh
                .observe(this, Observer {
                    if (shouldLoadEkikime()) {
                        viewModel.getAll()
                    } else {
                        viewModel.ekikimeList.value = ArrayList()
                        Toast.makeText(activity, "Sorry, you have to start sharing in order to start seeing ekiki me codes. Ki'king is caring...",
                                Toast.LENGTH_LONG)
                                .show()
                    }
                })

        viewModel.incompleteCard
                .observe(this, Observer {
                    Toast.makeText(activity,
                            "Recharge code is incomplete.",
                            Toast.LENGTH_SHORT).show()
                })

        viewModel.processInfo
                .observe(this, Observer {
                    loading.findViewById<TextView>(R.id.process_info)
                            .text = it
                })

        viewModel.saveShared.observe(this, Observer {
            saveSharedCard(it)
        })

        // now let the view model fetch the items
        viewModel.requestRefresh.call()

        askCallPermission()
    }

    private fun saveSharedCard(cardNumber: String?) {
        if (cardNumber == null) return

        val sharedCardPrefs = activity.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        val shared = sharedCardPrefs.getStringSet("shared", setOf("0", "1")) as HashSet<String>

        shared.add(cardNumber)
        sharedCardPrefs.edit()
                .putStringSet("shared", shared)
                .apply()
    }


    private fun getSharedCards(): MutableSet<String> {
        val sharedCardPrefs = activity.getSharedPreferences("statistics", Context.MODE_PRIVATE)

        // the reason for the default set of two items is to allow first two tries of loading
        return sharedCardPrefs.getStringSet("shared", setOf("0", "1"))
    }

    private fun shouldLoadEkikime(): Boolean {
        val shared = getSharedCards()
        val used = getUsedCards()

        return (shared.size - used.size) > 0
    }


    private fun askCallPermission() {
        if (!isCallGranted()) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), RC_CALL_PERM)
        }
    }

    private fun isCallGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
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
        emptyView.visibility = View.VISIBLE
    }

    private fun hideEmpty() {
        emptyView.visibility = View.GONE
    }


    private fun showLoading() {
        loading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loading.visibility = View.GONE
    }

    private fun loadCard(card: String?) {
        if (card == null) {
            return
        }

        if (card.length < 13) {
            Toast.makeText(activity, "Recharge code is incomplete.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!publishTipShown()) {
            AlertDialog.Builder(activity)
                    .setView(R.layout.publish_dialog)
                    .setPositiveButton("Continue to load credit") { dialog, _ ->
                        run {
                            dial(card)
                            dialog.dismiss()
                            activity.getSharedPreferences("ek_tip", Context.MODE_PRIVATE)
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

        if (!isCallGranted()) {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:*134*$card%23")))
        } else {
            val dialIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:*134*$card%23"))
            startActivity(dialIntent)
        }
    }

    private fun publishTipShown(): Boolean {
        return activity.getSharedPreferences("ek_tip", Context.MODE_PRIVATE)
                .getBoolean("ek_tip_shown", false)
    }

    private fun ekikime(card: String) {
        selectedCard = card

        // pre-process
        val usedCardsToday = getUsedCards()

        // restriction on twice a day is taken off
        /*if (usedCardsToday.size > 1) {
            Toast.makeText(context, "You cannot load Ekiki me twice a day. Please come back tomorrow.",
                    Toast.LENGTH_LONG).show()
            return
        }*/

        if (usedCardsToday.contains(card) or getSharedCards().contains(card)) {
            Toast.makeText(context, "You have already used this card, try another",
                    Toast.LENGTH_LONG).show()
            return
        }

        // show confirmation dialog
        val confirmDialog = BottomSheetDialog(context)
        val contentBinding: ViewUsedBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.view_used, null, false)

        confirmDialog.setContentView(contentBinding.root)
        confirmDialog.setCancelable(false)
        confirmDialog.show()

        val viewModel = ViewModelProviders.of(activity).get(HomeViewModel::class.java)
        contentBinding.yes.setOnClickListener {
            viewModel.use(card)
            confirmDialog.dismiss()

            saveToPrefs(card)
        }

        contentBinding.no.setOnClickListener {
            confirmDialog.dismiss()
        }

        contentBinding.invalidCard.setOnClickListener {
            viewModel.invalid(card)
            confirmDialog.dismiss()
        }

        if (!isCallGranted()) {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*135*$card%23"))
            startActivity(dialIntent)
        } else {
            val dialIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:*135*$card%23"))
            startActivity(dialIntent)
        }
    }

    // saves cards used today
    private fun saveToPrefs(card: String) {
        val sharedPreferences = activity.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        val cards = sharedPreferences.getStringSet("used", HashSet<String>()) as HashSet<String>

        cards.add(card)

        sharedPreferences.edit()
                .putStringSet("used", cards)
                .apply()
    }

    private fun getUsedCards(): HashSet<String> {
        val sharedPreferences = activity.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("used", HashSet<String>()) as HashSet<String>
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