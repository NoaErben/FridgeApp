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

    fun resetToDefaultItems() {
        val defaultFoodItems = listOf(
            FoodItem(name = "Bread", category = "Breads", daysToExpire = 8, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fbread.jpg?alt=media&token=4e3dac77-cba9-4ce3-bb62-d688e1f17924"),
            FoodItem(name = "Butter", category = "Dairy", daysToExpire = 21, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fbutter.jpg?alt=media&token=31f4452c-7b73-4045-b3f3-4307831783c4"),
            FoodItem(name = "Eggs", category = "Dairy", daysToExpire = 21, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fegg.jpg?alt=media&token=bfc528e0-981d-4c83-9277-56612a4aac4a"),
            FoodItem(name = "Milk", category = "Dairy", daysToExpire = 7, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fmilk.jpg?alt=media&token=f80dbc80-881a-4448-a764-ba3773cebf05"),
            FoodItem(name = "Yellow Cheese", category = "Dairy", daysToExpire = 10, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fcheese.jpeg?alt=media&token=38987267-9114-4f8e-8a28-e52d6b430187"),
            FoodItem(name = "Yogurt", category = "Dairy", daysToExpire = 7, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fyogurt.jpg?alt=media&token=4350d6f9-b41b-4622-b403-35dcfa0f5e45"),
            FoodItem(name = "Chicken", category = "Meat", daysToExpire = 7, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fchicken_breast.jpg?alt=media&token=312f8292-471f-4aee-9df1-1cc2f436b5e4"),
            FoodItem(name = "Ground Beef", category = "Meat", daysToExpire = 7, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fbeef.jpg?alt=media&token=0e12fcdf-e5ee-4fe8-8f80-327583d82a544"),
            FoodItem(name = "Carrot", category = "Vegetables", daysToExpire = 7, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fcarrot.jpg?alt=media&token=c000e72f-452e-4eaf-a10c-fbec75ba9a58"),
            FoodItem(name = "Cucumber", category = "Vegetables", daysToExpire = 7, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fcucumber.jpg?alt=media&token=a2266334-8d6c-4017-b87c-8cdac7d7a9ff"),
            FoodItem(name = "Lettuce", category = "Vegetables", daysToExpire = 7, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Flettuce.jpg?alt=media&token=509bbb4b-f4bf-444d-a29e-bb8c8a6bd768"),
            FoodItem(name = "Tomato", category = "Vegetables", daysToExpire = 7, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Ftomato.jpg?alt=media&token=790dbb85-7001-4dc0-905e-7bfd61142ebe"),
            FoodItem(name = "Salmon", category = "Fish", daysToExpire = 4, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fsalmon.jpg?alt=media&token=fbf445cb-cc0c-4680-9b07-edf6087c8b54"),
            FoodItem(name = "Ketchup", category = "Sauces", daysToExpire = 60, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fketchup.jpg?alt=media&token=01f3f0a1-06bd-44ec-a910-68ec42f2330a"),
            FoodItem(name = "Mayonnaise", category = "Sauces", daysToExpire = 15, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fmayonnaise.jpg?alt=media&token=444ee20c-38fc-421e-a930-ca903a9d0355"),
            FoodItem(name = "Mustard", category = "Sauces", daysToExpire = 30, photoUrl = "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fmustard.jpg?alt=media&token=f3173839-3080-4912-9bf5-2677464d852a")
        )
        viewModelScope.launch {
            foodRepository.deleteAll()
            foodRepository.insertAll(defaultFoodItems)
        }
    }

}