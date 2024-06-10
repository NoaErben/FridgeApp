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
    suspend fun insert(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("SELECT * FROM FoodItem")
    fun getAllFoodItems(): LiveData<List<FoodItem>>

    @Query("SELECT item_name FROM FoodItem")
    fun getFoodsNameList(): LiveData<List<String>>

    @Update
    suspend fun update(foodItem: FoodItem)

    @Query("UPDATE FoodItem SET item_name = :name WHERE id = :id")
    suspend fun updateName(id: Int, name: String)

    @Query("UPDATE FoodItem SET image_url = :photoUrl WHERE id = :id")
    suspend fun updatePhotoUrl(id: Int, photoUrl: String?)

    @Query("UPDATE FoodItem SET days_to_expire = :daysToExpire WHERE id = :id")
    suspend fun updateDaysToExpire(id: Int, daysToExpire: Int)

    @Query("UPDATE FoodItem SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Int, category: String)

    @Query("DELETE FROM FoodItem")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodItems: List<FoodItem>)

    @Query("SELECT COUNT(*) FROM FoodItem")
    suspend fun getCount(): Int

    @Query("SELECT * from FoodItem WHERE item_name LIKE :name")
    fun getFoodItem(name: String) : FoodItem
}
