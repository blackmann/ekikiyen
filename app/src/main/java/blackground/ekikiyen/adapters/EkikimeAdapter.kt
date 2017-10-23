package blackground.ekikiyen.adapters

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import blackground.ekikiyen.R
import blackground.ekikiyen.data.Ekikime
import blackground.ekikiyen.databinding.ViewEkikimeItemBinding

class EkikimeAdapter(private val items: ArrayList<Ekikime> = ArrayList()) : RecyclerView.Adapter<EkikimeAdapter.ViewHolder>() {
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val currentItem = items[position]
        holder?.bind(currentItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewEkikimeItemBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                        R.layout.view_ekikime_item, parent, false)

        return ViewHolder(binding)
    }


    fun update(list: ArrayList<Ekikime>?) {
        if (list == null) return

        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }


    class ViewHolder(val binding: ViewEkikimeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ekikime: Ekikime) {
            binding.ekikimeItem = ekikime
            binding.timeAgo.setText("")
            binding.usage.text = "used ${ekikime.usage} times already"
        }
    }
}