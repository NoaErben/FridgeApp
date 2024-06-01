package com.example.fridgeapp.data.ui

import FoodRepository
import FridgeRepository
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
import com.example.fridgeapp.data.repository.CartRepository
import kotlinx.coroutines.launch

class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val fridgeRepository = FridgeRepository(application)
    private val foodRepository = FoodRepository(application)
    private val cartRepository = CartRepository(application)

    val fridgeItems: LiveData<List<FridgeItem>>? = fridgeRepository.getAllFridgeItems()
    val expiringFridgeItems: LiveData<List<FridgeItem>>? = fridgeRepository.getExpiringFridgeItems(System.currentTimeMillis())
    val foodItems: LiveData<List<FoodItem>>? = foodRepository.getAllFoodItems()
    val foodItemsNames: LiveData<List<String>>? = foodRepository.getFoodsNameList()
    val cartItems: LiveData<List<CartItem>>? = cartRepository.getAllCartItems()

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

    fun insertFridgeItem(fridgeItem: FridgeItem) {
        fridgeRepository.insert(fridgeItem)
    }

    fun deleteFridgeItem(fridgeItem: FridgeItem) {
        fridgeRepository.delete(fridgeItem)
    }

    fun deleteFridgeItemByName(name: String) {
        fridgeRepository.deleteByName(name)
    }

    fun getFridgeItemById(id: Int): FridgeItem? {
        return fridgeRepository.getFridgeItemById(id)
    }

    fun getFridgeItemByName(name: String): FridgeItem? {
        return fridgeRepository.getFridgeItemByName(name)
    }

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


    fun deleteAllFridgeItems() {
        fridgeRepository.deleteAll()
    }

    fun deleteAllFoodItems() {
        foodRepository.deleteAllFoodTable()
    }

    fun insertCartItem(cartItem: CartItem) {
        cartRepository.insert(cartItem)
    }

    fun deleteCartItem(cartItem: CartItem) {
        cartRepository.delete(cartItem)
    }

    fun deleteAllCartItems() {
        cartRepository.deleteAll()
    }

    // Repository update functions

    fun updateFridgeItem(fridgeItem: FridgeItem) {
        fridgeRepository.update(fridgeItem)
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

    fun updateFridgeName(id: Int, name: String) {
        fridgeRepository.updateName(id, name)
    }

    fun updateFridgePhotoUrl(id: Int, photoUrl: String?) {
        fridgeRepository.updatePhotoUrl(id, photoUrl)
    }

    fun updateFridgeBuyingDate(id: Int, buyingDate: Long) {
        fridgeRepository.updateBuyingDate(id, buyingDate)
    }

    fun updateFridgeExpiryDate(id: Int, expiryDate: Long) {
        fridgeRepository.updateExpiryDate(id, expiryDate)
    }

    fun updateFridgeCountMeasure(id: Int, countMeasure: String) {
        fridgeRepository.updateCountMeasure(id, countMeasure)
    }

    fun updateFridgeCategory(id: Int, category: String) {
        fridgeRepository.updateCategory(id, category)
    }

    fun updateCartCategory(id: Int, category: String) {
        cartRepository.updateCategory(id, category)
    }

    fun updateCartRemarks(id: Int, remarks: String) {
        cartRepository.updateRemarks(id, remarks)
    }

    fun updateCartCount(id: Int, count: Int) {
        cartRepository.updateCartCount(id, count)
    }

    fun updateCartCountMeasure(id: Int, countMeasure: String) {
        cartRepository.updateCartCountMeasure(id, countMeasure)
    }

    fun updateCartImageUrl(id: Int, imageUrl: String?) {
        cartRepository.updateCartImageUrl(id, imageUrl)
    }

    fun insertCartItemFromFridgeItem(fridgeItem: FridgeItem) {
        cartRepository.insertCartItemFromFridgeItem(fridgeItem)
    }

}