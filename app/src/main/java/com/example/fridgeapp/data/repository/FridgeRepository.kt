package com.example.fridgeapp.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.model.FridgeItem
import com.google.firebase.auth.FirebaseUser

interface FridgeRepository {

    fun currentUser(): FirebaseUser?
    fun getItems(): LiveData<List<FridgeItem>>

    fun saveFridgeItemToDatabase(fridgeItem: FridgeItem, imageChanged: Boolean, imageUri: Uri?, context: Context,
                                          onComplete: (Result<Unit>) -> Unit)

    fun updateFridgeItemInDatabase(fridgeItem: FridgeItem, context: Context,
                                            photoUri: String?, onComplete: (Result<Unit>) -> Unit)

    fun deleteItemFromFridgeDatabase(fridgeItem: FridgeItem, onComplete: (Result<Unit>) -> Unit)

    fun deleteAllItemsFromFridgeDatabase(onComplete: (Result<Unit>) -> Unit)

    fun checkItemExists(itemName: String, callback: (Boolean) -> Unit)
}