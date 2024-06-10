package com.example.fridgeapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fridgeapp.data.model.CartItem

class ShoppingListViewModel : ViewModel() {

    private val _shoppingList = MutableLiveData<List<CartItem>>()
    val shoppingList: LiveData<List<CartItem>> get() = _shoppingList

    init {
        _shoppingList.value = listOf()
    }

    fun addItemToShoppingList(item: CartItem) {
        val updatedList = _shoppingList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(item)
        _shoppingList.value = updatedList
    }
}
