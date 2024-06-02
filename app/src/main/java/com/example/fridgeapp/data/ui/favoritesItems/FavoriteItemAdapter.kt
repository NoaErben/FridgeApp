package com.example.fridgeapp.data.ui.favoritesItems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.databinding.FavoriteItemCardBinding

class FavoriteItemAdapter(private var items: List<FoodItem>, private val callBack: ItemListener) : RecyclerView.Adapter<FavoriteItemAdapter.ItemViewHolder>(){

    interface ItemListener {
        fun onItemClick(index:Int)
        fun onItemLongClick(index:Int)
    }

    inner class ItemViewHolder(private val binding: FavoriteItemCardBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }
        fun bind(item: FoodItem) {
            print(item.photoUrl)
            binding.itemName.text = item.name
            binding.itemExpired.text = item.daysToExpire.toString() + " days"
            if (item.photoUrl != null)
                if (item.photoUrl.contains("drawable"))
                    Glide.with(binding.root).load(item.photoUrl.substringAfter("drawable://").toInt()).circleCrop().into(binding.itemImage)
                else
                    Glide.with(binding.root).load(item.photoUrl).circleCrop().error(R.drawable.dish).into(binding.itemImage)
            else
                Glide.with(binding.root).load(ContextCompat.getDrawable(binding.root.context,
                    R.drawable.dish
                )).circleCrop().into(binding.itemImage)

        }

        override fun onClick(p0: View?) {
            callBack.onItemClick(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClick(adapterPosition)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemViewHolder(FavoriteItemCardBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    fun itemAt(index: Int) = items[index]
    fun setItems(newItems: List<FoodItem>?) {
        newItems?.let {
            items = it
            notifyDataSetChanged()
        }
    }

}