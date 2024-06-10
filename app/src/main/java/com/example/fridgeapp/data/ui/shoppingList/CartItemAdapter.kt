package com.example.fridgeapp.data.ui.shoppingList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.databinding.CartLayoutBinding

class CartItemAdapter(private val items: List<CartItem>) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = CartLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CartItemViewHolder(private val binding: CartLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            binding.itemName.text = item.name
            binding.itemCategory.text = item.category
            binding.itemQuantity.text = "Quantity: ${item.quantity}"
            if (item.photoUrl != null) {
                Glide.with(binding.itemImage.context)
                    .load(item.photoUrl)
                    .into(binding.itemImage)
            } else {
                binding.itemImage.setImageResource(R.drawable.dish) // Default image
            }
        }
    }
}
