package com.example.fridgeapp.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FridgeItem
import com.google.firebase.auth.FirebaseUser

interface CartRepository {

    fun currentUser(): FirebaseUser?
    fun getCartItems(): LiveData<List<CartItem>>
    fun getFridgeItems(): LiveData<List<FridgeItem>>

    suspend fun saveCartItemToDatabase(cartItem: CartItem, imageChanged: Boolean, imageUri: Uri?, context: Context,
                                 onComplete: (Result<Unit>) -> Unit)

    suspend fun updateCartItemInDatabase(cartItem: CartItem, context: Context,
                                   photoUri: String?, onComplete: (Result<Unit>) -> Unit)

    suspend fun deleteItemFromCartDatabase(cartItem: CartItem, onComplete: (Result<Unit>) -> Unit)
    suspend fun deleteItemFromFridgeDatabase(fridgeItem: FridgeItem, onComplete: (Result<Unit>) -> Unit)

    suspend fun deleteAllItemsFromCartDatabase(onComplete: (Result<Unit>) -> Unit)

    suspend fun checkCartItemExists(itemName: String, callback: (Boolean) -> Unit)
}