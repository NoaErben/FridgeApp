package com.example.fridgeapp.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.model.FridgeItem
import kotlinx.coroutines.runBlocking

@Database(entities = arrayOf(FoodItem::class, FridgeItem::class, CartItem::class), version = 1, exportSchema = false)
abstract class FridgeDB : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun fridgeDao(): FridgeDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var instance: FridgeDB? = null

        fun getDatabase(context: Context): FridgeDB {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FridgeDB::class.java,
                        "fridge_database"
                    )
                        .allowMainThreadQueries() // Allows Room to perform database operations on the main thread
                        .build()

                    // Default items for foodDB
                    instance?.let { database ->
                        runBlocking {
                            foodTableDefaultValues(database.foodDao())
                        }
                    }
                }
            }
            return instance!!
        }

        private suspend fun foodTableDefaultValues(foodDao: FoodDao) {
            val count = foodDao.getCount()
            if (count == 0) {
                // Default items for foodDB
                val defaultFoodItems = listOf(
                    FoodItem(name = "Bread", category = "Breads", daysToExpire = 8, photoUrl = "content://com.example.app.provider/images/bread.jpg"),
                    FoodItem(name = "Butter", category = "Diary", daysToExpire = 21, photoUrl = "content://com.example.app.provider/images/butter.jpg"),
                    FoodItem(name = "Eggs", category = "Diary", daysToExpire = 21, photoUrl = "content://com.example.app.provider/images/egg.jpg"),
                    FoodItem(name = "Milk", category = "Diary", daysToExpire = 7, photoUrl = "content://com.example.app.provider/images/milk.jpg"),
                    FoodItem(name = "Yellow Cheese", category = "Diary", daysToExpire = 10, photoUrl = "content://com.example.app.provider/images/cheese.jpg"),
                    FoodItem(name = "Yogurt", category = "Diary", daysToExpire = 7, photoUrl = "content://com.example.app.provider/images/yogurt.jpg"),
                    FoodItem(name = "Chicken", category = "Meat", daysToExpire = 7, photoUrl = "content://com.example.app.provider/images/chicken_breast.jpg"),
                    FoodItem(name = "Ground Beef", category = "Meat", daysToExpire = 7, photoUrl = "content://com.example.app.provider/images/beef.jpg"),
                    FoodItem(name = "Carrot", category = "Vegetables", daysToExpire = 7, photoUrl = "content://com.example.app.provider/images/carrot.jpg"),
                    FoodItem(name = "Cucumber", category = "Vegetables", daysToExpire = 7, photoUrl = "content://com.example.app.provider/images/cucumber.jpg"),
                    FoodItem(name = "Lettuce", category = "Vegetables", daysToExpire = 7, photoUrl = "content://com.example.app.provider/images/lettuce.jpg"),
                    FoodItem(name = "Tomato", category = "Vegetables", daysToExpire = 7, photoUrl = "content://com.example.app.provider/images/tomato.jpg"),
                    FoodItem(name = "Salmon", category = "Fish", daysToExpire = 4, photoUrl = "content://com.example.app.provider/images/salmon.jpg"),
                    FoodItem(name = "Ketchup", category = "Sauces", daysToExpire = 60, photoUrl = "content://com.example.app.provider/images/ketchup.jpg"),
                    FoodItem(name = "Mayonnaise", category = "Sauces", daysToExpire = 15, photoUrl = "content://com.example.app.provider/images/mayonnaise.jpg"),
                    FoodItem(name = "Mustard", category = "Sauces", daysToExpire = 30, photoUrl = "content://com.example.app.provider/images/mustard.jpg")
                )
                foodDao.insertAll(defaultFoodItems)
            }
        }
    }
}
