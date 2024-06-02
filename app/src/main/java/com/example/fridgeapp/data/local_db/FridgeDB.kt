package com.example.fridgeapp.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.model.FridgeItem
import kotlinx.coroutines.runBlocking

@Database(entities = arrayOf(FoodItem::class), version = 1, exportSchema = false)
abstract class FridgeDB : RoomDatabase() {

    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var instance: FridgeDB? = null

        fun getDatabase(context: Context): FridgeDB {
            //context.deleteDatabase("fridge_database")
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, FridgeDB::class.java, "fridge_database"
                    ).build()

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
                    FoodItem(name = "Bread", category = "Breads", daysToExpire = 8, photoUrl = "drawable://" + R.drawable.bread.toString()),
                    FoodItem(name = "Butter", category = "Diary", daysToExpire = 21, photoUrl = "drawable://" + R.drawable.butter.toString()),
                    FoodItem(name = "Eggs", category = "Diary", daysToExpire = 21, photoUrl = "drawable://" + R.drawable.egg.toString()),
                    FoodItem(name = "Milk", category = "Diary", daysToExpire = 7, photoUrl = "drawable://" + R.drawable.milk.toString()),
                    FoodItem(name = "Yellow Cheese", category = "Diary", daysToExpire = 10, photoUrl = "drawable://" + R.drawable.cheese.toString()),
                    FoodItem(name = "Yogurt", category = "Diary", daysToExpire = 7, photoUrl = "drawable://" + R.drawable.yogurt.toString()),
                    FoodItem(name = "Chicken", category = "Meat", daysToExpire = 7, photoUrl = "drawable://" + R.drawable.chicken_breast.toString()),
                    FoodItem(name = "Ground Beef", category = "Meat", daysToExpire = 7, photoUrl = "drawable://" + R.drawable.beef.toString()),
                    FoodItem(name = "Carrot", category = "Vegetables", daysToExpire = 7, photoUrl = "drawable://" + R.drawable.carrot.toString()),
                    FoodItem(name = "Cucumber", category = "Vegetables", daysToExpire = 7, photoUrl = "drawable://" + R.drawable.cucumber.toString()),
                    FoodItem(name = "Lettuce", category = "Vegetables", daysToExpire = 7, photoUrl = "drawable://" + R.drawable.lettuce.toString()),
                    FoodItem(name = "Tomato", category = "Vegetables", daysToExpire = 7, photoUrl = "drawable://" + R.drawable.tomato.toString()),
                    FoodItem(name = "Salmon", category = "Fish", daysToExpire = 4, photoUrl = "drawable://" + R.drawable.salmon.toString()),
                    FoodItem(name = "Ketchup", category = "Sauces", daysToExpire = 60, photoUrl = "drawable://" + R.drawable.ketchup.toString()),
                    FoodItem(name = "Mayonnaise", category = "Sauces", daysToExpire = 15, photoUrl = "drawable://" + R.drawable.mayonnaise.toString()),
                    FoodItem(name = "Mustard", category = "Sauces", daysToExpire = 30, photoUrl = "drawable://" + R.drawable.mustard.toString())
                )
                foodDao.insertAll(defaultFoodItems)
            }
            //else
                //foodDao.deleteAll()
        }
    }
}
