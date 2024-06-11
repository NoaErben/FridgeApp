package com.example.fridgeapp.data.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.FridgeRepository
import com.google.firebase.auth.FirebaseAuth

class FridgeLiveDataViewModel : ViewModel() {

    private val repository = FridgeRepository()
    val items: LiveData<List<FridgeItem>> = repository.getItems()

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    init {
        loadCategories()
        items.observeForever { itemList ->
            Log.d("MyTag", "Items fetched in ViewModel: ${itemList.size}")
            itemList.forEach {
                Log.d("MyTag", "Item: ${it.name}, Expiry: ${it.expiryDate}, Photo: ${it.photoUrl}")
            }
        }
    }

    private fun loadCategories() {
        // Load categories from your data source
        _categories.value = listOf(
            "Vegetables",
            "Fruits",
            "Dairy",
            "Meat",
            "Fish",
            "Beverages",
            "Snacks",
            "Other"
        )
    }

    fun isUserLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser != null
    }
}
