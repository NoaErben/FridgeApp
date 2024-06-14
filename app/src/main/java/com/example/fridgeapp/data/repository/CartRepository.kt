package com.example.fridgeapp.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FridgeItem
import com.google.firebase.auth.FirebaseUser

interface CartRepository {

    fun currentUser(): FirebaseUser?
    fun getItems(): LiveData<List<CartItem>>

    fun saveCartItemToDatabase(cartItem: CartItem, imageChanged: Boolean, imageUri: Uri?, context: Context,
                                 onComplete: (Result<Unit>) -> Unit)

    fun updateCartItemInDatabase(cartItem: CartItem, context: Context,
                                   photoUri: String?, onComplete: (Result<Unit>) -> Unit)

    fun deleteItemFromCartDatabase(cartItem: CartItem, onComplete: (Result<Unit>) -> Unit)

    fun deleteAllItemsFromCartDatabase(onComplete: (Result<Unit>) -> Unit)

    fun checkCartItemExists(itemName: String, callback: (Boolean) -> Unit)
}