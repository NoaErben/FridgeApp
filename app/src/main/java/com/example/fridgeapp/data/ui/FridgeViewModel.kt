package com.example.fridgeapp.data.ui

import FoodRepository
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.model.FridgeItem
import kotlinx.coroutines.launch

class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val foodRepository = FoodRepository(application)

    val foodItems: LiveData<List<FoodItem>>? = foodRepository.getAllFoodItems()
    val foodItemsNames: LiveData<List<String>>? = foodRepository.getFoodsNameList()

    val categories = listOf("Breads", "Dairy", "Vegetables", "Meat", "Sauces", "Fish")

    val stringListLiveData: LiveData<List<String>>? get() = foodItemsNames

    // Function to get the concatenated string
    fun getConcatenatedString(): String {
        //foodRepository.deleteAllFoodTable()
        return foodItemsNames?.value?.joinToString(separator = ", ") ?: ""
    }

    fun getImageResource(context: Context, photoUrl: String): Int {
        return context.resources.getIdentifier(photoUrl, "drawable", context.packageName)
    }

    private val _chosenFridgeItem = MutableLiveData<FridgeItem>()
    val chosenFridgeItem: LiveData<FridgeItem> get() = _chosenFridgeItem

    private val _chosenFoodItem = MutableLiveData<FoodItem>()
    val chosenFoodItem: LiveData<FoodItem> get() = _chosenFoodItem

    private val _chosenCartItem = MutableLiveData<CartItem>()
    val chosenCartItem: LiveData<CartItem> get() = _chosenCartItem

    fun setFridgeChosenItem(fridgeItem: FridgeItem) {
        _chosenFridgeItem.value = fridgeItem
    }

    fun setFoodChosenItem(foodItem: FoodItem) {
        _chosenFoodItem.value = foodItem
    }

    fun setCartChosenItem(cartItem: CartItem) {
        _chosenCartItem.value = cartItem
    }

    // Methods to use repository functions
    fun insertFoodItem(foodItem: FoodItem) {
        foodRepository.insert(foodItem)
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRepository.delete(foodItem)
            Log.d("FridgeViewModel", "Food item deleted from repository")
        }
    }

    fun deleteFoodItemByName(name: String) {
        foodRepository.deleteByName(name)
    }

    fun getFoodItemById(id: Int): FoodItem? {
        return foodRepository.getFoodItemById(id)
    }

    fun getFoodItemByName(name: String): FoodItem? {
        return foodRepository.getFoodItemByName(name)
    }

    fun updateFoodItem(foodItem: FoodItem) {
        _chosenFoodItem.value = foodItem
        viewModelScope.launch {
            Log.d("FridgeViewModel", "Updating food item: $foodItem")
            foodRepository.update(foodItem)
            Log.d("FridgeViewModel", "Food item updated in repository")
        }
    }


    fun deleteAllFoodItems() {
        foodRepository.deleteAllFoodTable()
    }

    fun updateFoodName(id: Int, name: String) {
        foodRepository.updateName(id, name)
    }

    fun updateFoodCategory(id: Int, newCategory: String) {
        foodRepository.updateCategory(id, newCategory)
    }

    fun updateFoodPhotoUrl(id: Int, photoUrl: String?) {
        foodRepository.updatePhotoUrl(id, photoUrl)
    }

    fun updateFoodDaysToExpire(id: Int, daysToExpire: Int) {
        foodRepository.updateDaysToExpire(id, daysToExpire)
    }
    
}