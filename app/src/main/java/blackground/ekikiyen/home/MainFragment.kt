package blackground.ekikiyen.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import blackground.ekikiyen.R
import blackground.ekikiyen.adapters.EkikimeAdapter
import blackground.ekikiyen.data.Ekikime
import blackground.ekikiyen.databinding.ViewUsedBinding
import java.util.*

class MainFragment : Fragment() {

    private lateinit var adapter: EkikimeAdapter
    private var selectedCard = ""


    // views
    private lateinit var list: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var loading: LinearLayout

    companion object {
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
                .observe(this, Observer { viewModel.getAll() })

        viewModel.incompleteCard
                .observe(this, Observer { Toast.makeText(activity,
                        "Recharge code is incomplete.",
                        Toast.LENGTH_SHORT).show() })

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

        if (card.length < 14) {
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

        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*134*$card%23"))
        startActivity(dialIntent)
    }

    private fun publishTipShown(): Boolean {
        return activity.getSharedPreferences("ek_tip", Context.MODE_PRIVATE)
                .getBoolean("ek_tip_shown", false)
    }

    private fun ekikime(card: String) {
        selectedCard = card

        // pre-process
        val usedCardsToday = getUsedCards()
        if (usedCardsToday.size > 1) {
            Toast.makeText(context, "You cannot load Ekiki me twice a day. Please come back tomorrow.",
                    Toast.LENGTH_LONG).show()
            return
        }

        if (usedCardsToday.contains(card)) {
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

        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*135*$card%23"))
        startActivity(dialIntent)
    }

    private fun saveToPrefs(card: String) {
        val sharedPreferences = activity.getSharedPreferences(Date().getDateString(), Context.MODE_PRIVATE)
        val cards = sharedPreferences.getStringSet("today", HashSet<String>()) as HashSet<String>

        cards.add(card)

        sharedPreferences.edit()
                .putStringSet("today", cards)
                .apply()
    }

    private fun getUsedCards(): HashSet<String> {
        val sharedPreferences = activity.getSharedPreferences(Date().getDateString(), Context.MODE_PRIVATE)
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