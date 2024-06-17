package com.example.fridgeapp.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.model.FridgeItem
import com.google.firebase.auth.FirebaseUser

/**
 * Interface for fridge-related operations.
 */
interface FridgeRepository {

    fun currentUser(): FirebaseUser?
    fun currentUserId(): String?
    fun getItems(): LiveData<List<FridgeItem>>

    suspend fun saveFridgeItemToDatabase(fridgeItem: FridgeItem, imageChanged: Boolean, imageUri: Uri?, context: Context,
                                          onComplete: (Result<Unit>) -> Unit)

    suspend fun updateFridgeItemInDatabase(fridgeItem: FridgeItem, context: Context,
                                            photoUri: String?, onComplete: (Result<Unit>) -> Unit)

    suspend fun deleteItemFromFridgeDatabase(fridgeItem: FridgeItem, onComplete: (Result<Unit>) -> Unit)

    suspend fun deleteAllItemsFromFridgeDatabase(onComplete: (Result<Unit>) -> Unit)

    suspend fun checkItemExists(itemName: String, callback: (Boolean) -> Unit)
}