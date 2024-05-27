package com.example.fridgeapp.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fridgeapp.data.model.FoodItem

@Dao
interface FoodDao {

    @Insert
    fun insert(foodItem: FoodItem)

    @Delete
    fun delete(foodItem: FoodItem)

    @Query("DELETE FROM FoodItem WHERE item_name = :name")
    fun deleteByName(name: String)

    @Query("SELECT * FROM FoodItem")
    fun getAllFoodItems(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM FoodItem WHERE id = :id")
    fun getFoodItemById(id: Int): FoodItem?

    @Query("SELECT * FROM FoodItem WHERE item_name = :name")
    fun getFoodItemByName(name: String): FoodItem?

    @Query("SELECT item_name FROM FoodItem")
    fun getFoodsNameList(): LiveData<List<String>>

    @Update
    fun update(foodItem: FoodItem)

    @Query("UPDATE FoodItem SET item_name = :name WHERE id = :id")
    fun updateName(id: Int, name: String)

    @Query("UPDATE FoodItem SET image_url = :photoUrl WHERE id = :id")
    fun updatePhotoUrl(id: Int, photoUrl: String?)

    @Query("UPDATE FoodItem SET days_to_expire = :daysToExpire WHERE id = :id")
    fun updateDaysToExpire(id: Int, daysToExpire: Int)

    @Query("UPDATE FoodItem SET category = :category WHERE id = :id")
    fun updateCategory(id: Int, category: String)

    @Query("DELETE FROM FoodItem")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodItems: List<FoodItem>)
}
