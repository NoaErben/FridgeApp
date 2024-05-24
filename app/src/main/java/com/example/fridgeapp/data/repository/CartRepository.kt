package com.example.fridgeapp.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.local_db.CartDao
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FridgeItem

class CartRepository(application: Application) {

    private var cartDao: CartDao?

    init {
        val db = FridgeDB.getDatabase(application.applicationContext)
        cartDao = db?.cartDao()
    }

        fun insert(cartItem: CartItem) {
            cartDao?.insert(cartItem)
    }

    fun delete(cartItem: CartItem) {
        cartDao?.delete(cartItem)
    }

    fun deleteByName(name: String) {
        cartDao?.deleteByName(name)
    }

    fun getAllCartItems(): LiveData<List<CartItem>>? {
        return cartDao?.getAllCartItems()
    }

    fun getCartItemById(id: Int): CartItem? {
        return cartDao?.getCartItemById(id)
    }

    fun getCartItemByName(name: String): CartItem? {
        return cartDao?.getCartItemByName(name)
    }

    fun getCartItemsNameList(): LiveData<List<String>>? {
        return cartDao?.getCartItemsNameList()
    }

    fun update(cartItem: CartItem) {
        cartDao?.update(cartItem)
    }

    fun updateName(id: Int, name: String) {
        cartDao?.updateName(id, name)
    }

    fun updateCount(id: Int, count: Int) {
        cartDao?.updateCount(id, count)
    }

    fun updateCountMeasure(id: Int, countMeasure: String) {
        cartDao?.updateCountMeasure(id, countMeasure)
    }

    fun updateImageUrl(id: Int, photoUrl: String?) {
        cartDao?.updateImageUrl(id, photoUrl)
    }

    fun updateCategory(id: Int, category: String) {
        cartDao?.updateCategory(id, category)
    }

    fun updateRemarks(id: Int, remarks: String) {
        cartDao?.updateRemarks(id, remarks)
    }

    fun updateCartCount(id: Int, count: Int) {
        cartDao?.updateCount(id, count)
    }

    fun updateCartCountMeasure(id: Int, countMeasure: String) {
        cartDao?.updateCountMeasure(id, countMeasure)
    }

    fun updateCartImageUrl(id: Int, imageUrl: String?) {
        cartDao?.updateImageUrl(id, imageUrl)
    }

    fun deleteAll() {
        cartDao?.deleteAll()
    }

    fun insertCartItemFromFridgeItem(fridgeItem: FridgeItem) {
        val cartItem = CartItem(
            name = fridgeItem.name,
            count = fridgeItem.count,

            // TODO: maybe popup to chose count??

            countMeasure = fridgeItem.countMeasure,
            photoUrl = fridgeItem.photoUrl,
            category = fridgeItem.category,
            remarks = "" // TODO: Add default remarks or leave it blank or popup
        )
        cartDao?.insert(cartItem)
    }
}
