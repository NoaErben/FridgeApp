package com.example.fridgeapp.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.model.FridgeItem

@Database(entities = arrayOf(FoodItem::class, FridgeItem::class), version = 1, exportSchema = false)
abstract class FridgeDB : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun fridgeDao(): FridgeDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var instance: FridgeDB? = null

        fun getDatabase(context: Context): FridgeDB? {

            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FridgeDB::class.java,
                        "fridge_database"
                    ).build()
                }
            }
            return instance
        }
    }
}
