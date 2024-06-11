package com.example.fridgeapp.data.ui.viewModels

import FoodRepository
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.model.FridgeItem
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val foodRepository = FoodRepository(application)

    private val _chosenFoodItem = MutableLiveData<FoodItem>()
    private val _categories = listOf("Breads", "Dairy", "Vegetables", "Meat", "Sauces", "Fish", "Other")
    private val _unitMeasures = listOf("Grams", "Kilograms", "Milliliters", "Liters", "Pieces", "Packets", "Boxes")

    val foodItems: LiveData<List<FoodItem>>? = foodRepository.getAllFoodItems()
    val foodItemsNames: LiveData<List<String>>? = foodRepository.getFoodsNameList()
    val categories get() = _categories
    val unitMeasures get() = _unitMeasures

    val chosenFoodItem: LiveData<FoodItem> get() = _chosenFoodItem

    fun setFoodChosenItem(foodItem: FoodItem) {
        _chosenFoodItem.value = foodItem
    }

    // ################## Room functions ##################

    fun insertFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRepository.insert(foodItem)
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRepository.delete(foodItem)
            Log.d("FridgeViewModel", "Food item deleted from repository")
        }
    }

    fun deleteAllFoodItems() {
        // TODO: Add this option
        viewModelScope.launch {
            foodRepository.deleteAllFoodTable()
        }
    }

    fun updateFoodName(id: Int, name: String) {
        viewModelScope.launch {
            foodRepository.updateName(id, name)
        }
    }

    fun updateFoodCategory(id: Int, newCategory: String) {
        viewModelScope.launch {
            foodRepository.updateCategory(id, newCategory)
        }

    }

    fun updateFoodPhotoUrl(id: Int, photoUrl: String?) {
        viewModelScope.launch {
            foodRepository.updatePhotoUrl(id, photoUrl)
        }
    }

    fun updateFoodDaysToExpire(id: Int, daysToExpire: Int) {
        viewModelScope.launch {
            foodRepository.updateDaysToExpire(id, daysToExpire)
        }
    }

    suspend fun getFoodItem(name: String): FoodItem? {
        return withContext(Dispatchers.IO) {
            foodRepository.getFoodItem(name)
        }
    }

}