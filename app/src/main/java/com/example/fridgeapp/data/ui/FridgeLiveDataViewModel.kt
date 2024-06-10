package com.example.fridgeapp.data.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.FridgeRepository
import com.google.firebase.auth.FirebaseAuth

class FridgeLiveDataViewModel : ViewModel() {

    private val repository = FridgeRepository()
    val items: LiveData<List<FridgeItem>> = repository.getItems()

    init {
        items.observeForever { itemList ->
            Log.d("MyTag", "Items fetched in ViewModel: ${itemList.size}")
            itemList.forEach {
                Log.d("MyTag", "Item: ${it.name}, Expiry: ${it.expiryDate}, Photo: ${it.photoUrl}")
            }
        }
    }


//    val categories = listOf("Breads", "Dairy", "Vegetables", "Meat", "Sauces", "Fish")

    fun isUserLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser != null
    }
}