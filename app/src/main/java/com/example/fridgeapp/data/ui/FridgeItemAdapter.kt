package com.example.fridgeapp.data.ui
import com.bumptech.glide.Glide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.databinding.FridgeFragmentBinding

class FridgeItemAdapter(private val items:List<FridgeItem>, private val callBack: ItemListener) : RecyclerView.Adapter<FridgeItemAdapter.ItemViewHolder>(){

    interface ItemListener {
        fun onItemClick(index:Int)
        fun onItemLongClick(index:Int)
    }

    inner class ItemViewHolder(private val binding: FridgeFragmentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }
        fun bind(item: FridgeItem) {
            binding.itemName.text = item.name
            val currentTime = System.currentTimeMillis() // Current time in milliseconds
            //Calculate milliseconds until expiry milliseconds and convert it to days
            val daysUntilExpiry = (item.expiryDate - currentTime) / (1000 * 60 * 60 * 24)
            binding.itemExpired.text = "Expired in: ${daysUntilExpiry} days"

            Glide.with(binding.root).load(item.photoUrl).circleCrop().into(binding.itemImage)
        }

        override fun onClick(p0: View?) {
            callBack.onItemClick(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClick(adapterPosition)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = FridgeFragmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    fun itemAt(index: Int) = items[index]
}