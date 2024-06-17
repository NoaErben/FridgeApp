package com.example.fridgeapp.data.repository.roomImpl

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.local_db.FoodDao
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.repository.FoodRepository

/**
 * Implementation of [FoodRepository] that uses Room for local data storage.
 */
class FoodRepositoryRoom(application: Application) : FoodRepository {

    private var foodDao: FoodDao?

    init {
        val db = FridgeDB.getDatabase(application.applicationContext)
        foodDao = db.foodDao()
    }

    override fun getAllFoodItems(): LiveData<List<FoodItem>>? {
        return foodDao?.getAllFoodItems()
    }

    override suspend fun insert(foodItem: FoodItem) {
        foodDao?.insert(foodItem)
    }

    override suspend fun insertAll(foodItems: List<FoodItem>){
        foodDao?.insertAll(foodItems)
    }

    override suspend fun deleteAll(){
        foodDao?.deleteAll()
    }

    override suspend fun delete(foodItem: FoodItem) {
        try {
            foodDao?.delete(foodItem)
        } catch (e: Exception) {
            Log.e("FoodRepository", "Delete failed", e)
        }
    }


    override fun getFoodsNameList(): LiveData<List<String>>? {
        return foodDao?.getFoodsNameList()
    }

    override suspend fun update(foodItem: FoodItem) {
        try {
            foodDao?.update(foodItem)
        } catch (e: Exception) {
            Log.e("FoodRepository", "Update failed", e)
        }
    }

    override suspend fun updateName(id: Int, name: String) {
        foodDao?.updateName(id, name)
    }

    override suspend fun updatePhotoUrl(id: Int, photoUrl: String?) {
        foodDao?.updatePhotoUrl(id, photoUrl)
    }

    override suspend fun updateDaysToExpire(id: Int, daysToExpire: Int) {
        foodDao?.updateDaysToExpire(id, daysToExpire)
    }

    override suspend fun deleteAllFoodTable() {
        foodDao?.deleteAll()
    }

    override suspend fun updateCategory(id: Int, newCategory: String) {
        foodDao?.updateCategory(id, newCategory)
    }

    override suspend fun getFoodItem(name: String): FoodItem? {
        return foodDao?.getFoodItem(name)
    }

}