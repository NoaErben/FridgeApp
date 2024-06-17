package com.example.fridgeapp.data.repository

import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.model.FoodItem

/**
 * Interface for food-related operations.
 */
interface FoodRepository {

    fun getAllFoodItems(): LiveData<List<FoodItem>>?
    suspend fun insert(foodItem: FoodItem)
    suspend fun insertAll(foodItems: List<FoodItem>)
    suspend fun deleteAll()
    suspend fun delete(foodItem: FoodItem)
    fun getFoodsNameList(): LiveData<List<String>>?
    suspend fun update(foodItem: FoodItem)
    suspend fun updateName(id: Int, name: String)
    suspend fun updatePhotoUrl(id: Int, photoUrl: String?)
    suspend fun updateDaysToExpire(id: Int, daysToExpire: Int)
    suspend fun deleteAllFoodTable()
    suspend fun updateCategory(id: Int, newCategory: String)
    suspend fun getFoodItem(name: String): FoodItem?

}