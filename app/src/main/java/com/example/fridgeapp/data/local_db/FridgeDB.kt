package com.example.fridgeapp.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.ui.utils.DeafultFoodList
import kotlinx.coroutines.runBlocking

/**
 * The Room database for this app, which contains the FoodItem entity.
 * Used for saving the favorite item of the user of each phone.
 */
@Database(entities = arrayOf(FoodItem::class), version = 1, exportSchema = false)
abstract class FridgeDB : RoomDatabase() {

    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var instance: FridgeDB? = null

        fun getDatabase(context: Context): FridgeDB {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, FridgeDB::class.java, "fridge_database"
                    ).build()

                    // Default items for foodDB
                    instance?.let { database ->
                        runBlocking {
                            foodTableDefaultValues(database.foodDao(), context)
                        }
                    }
                }
            }
            return instance!!
        }

        private suspend fun foodTableDefaultValues(foodDao: FoodDao, context: Context) {
            val count = foodDao.getCount()
            if (count == 0) {
                // Default items for foodDB
                val defaultFoodItems = DeafultFoodList.createDefaultFoodItems(context)
                foodDao.insertAll(defaultFoodItems)
            }
        }
    }
}
