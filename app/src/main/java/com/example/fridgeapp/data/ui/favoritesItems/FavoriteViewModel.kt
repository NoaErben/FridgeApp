package com.example.fridgeapp.data.ui.favoritesItems

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.repository.FoodRepository
import com.example.fridgeapp.data.ui.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteViewModel(private val foodRep: FoodRepository) : ViewModel() {

//    private val foodRepositoryOld = FoodRepositoryOld(application)

    private val _chosenFoodItem = MutableLiveData<FoodItem>()

    val foodItems: LiveData<List<FoodItem>>? = foodRep.getAllFoodItems()
    val foodItemsNames: LiveData<List<String>>? = foodRep.getFoodsNameList()
    val categories get() = Constants.categories
    val unitMeasures get() = Constants.unitMeasures

    val chosenFoodItem: LiveData<FoodItem> get() = _chosenFoodItem

    fun setFoodChosenItem(foodItem: FoodItem) {
        _chosenFoodItem.value = foodItem
    }

    // ################## Room functions ##################

    fun insertFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRep.insert(foodItem)
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRep.delete(foodItem)
            Log.d("FridgeViewModel", "Food item deleted from repository")
        }
    }

    fun deleteAllFoodItems() {
        // TODO: Add this option
        viewModelScope.launch {
            foodRep.deleteAllFoodTable()
        }
    }

    fun updateFoodName(id: Int, name: String) {
        viewModelScope.launch {
            foodRep.updateName(id, name)
        }
    }

    fun updateFoodCategory(id: Int, newCategory: String) {
        viewModelScope.launch {
            foodRep.updateCategory(id, newCategory)
        }

    }

    fun updateFoodPhotoUrl(id: Int, photoUrl: String?) {
        viewModelScope.launch {
            foodRep.updatePhotoUrl(id, photoUrl)
        }
    }

    fun updateFoodDaysToExpire(id: Int, daysToExpire: Int) {
        viewModelScope.launch {
            foodRep.updateDaysToExpire(id, daysToExpire)
        }
    }

    suspend fun getFoodItem(name: String): FoodItem? {
        return withContext(Dispatchers.IO) {
            foodRep.getFoodItem(name)
        }
    }

    fun resetToDefaultItems() {
        val defaultFoodItems = Constants.defaultFoodItems
        viewModelScope.launch {
            foodRep.deleteAll()
            foodRep.insertAll(defaultFoodItems)
        }
    }

    class FavoriteViewModelFactory(private val repo: FoodRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoriteViewModel(repo) as T
        }
    }

}