package com.example.fridgeapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fridgeapp.data.model.FridgeItem

class FridgeLiveDataViewModel : ViewModel() {

    private val _items = MutableLiveData<List<FridgeItem>>(mutableListOf())
    val items: LiveData<List<FridgeItem>> get() = _items

    fun addItem(item: FridgeItem) {
        val currentItems = _items.value.orEmpty().toMutableList()
        currentItems.add(item)
        _items.value = currentItems
    }


}