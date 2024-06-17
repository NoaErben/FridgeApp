package com.example.fridgeapp.data.local_db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.ui.utils.Constants
import com.example.fridgeapp.data.ui.utils.DeafultFoodList
import kotlinx.coroutines.runBlocking

@Database(entities = arrayOf(FoodItem::class), version = 1, exportSchema = false)
abstract class FridgeDB : RoomDatabase() {

    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var instance: FridgeDB? = null

        fun getDatabase(context: Context): FridgeDB {
//            context.deleteDatabase("fridge_database")
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
//                val breadID = context.resources.getIdentifier("bread", "drawable", context?.packageName)
//                Log.d("MyTag", breadID.toString())
//                Log.d("MyTag",R.drawable.bread.toString())

                // Default items for foodDB
                val defaultFoodItems = DeafultFoodList.createDefaultFoodItems(context)
                foodDao.insertAll(defaultFoodItems)
            }
            //else
                //foodDao.deleteAll()
        }
    }
}
