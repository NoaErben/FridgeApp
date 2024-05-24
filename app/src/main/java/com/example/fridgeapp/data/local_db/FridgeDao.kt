package com.example.fridgeapp.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fridgeapp.data.model.FridgeItem

@Dao
interface FridgeDao {

    @Insert
    fun insert(fridgeItem: FridgeItem)

    @Delete
    fun delete(fridgeItem: FridgeItem)

    @Query("DELETE FROM FridgeItem WHERE item_name = :name")
    fun deleteByName(name: String)

    @Query("SELECT * FROM FridgeItem")
    fun getAllFridgeItems(): LiveData<List<FridgeItem>>

    @Query("SELECT * FROM FridgeItem WHERE id = :id")
    fun getFridgeItemById(id: Int): FridgeItem?

    @Query("SELECT * FROM FridgeItem WHERE item_name = :name")
    fun getFridgeItemByName(name: String): FridgeItem?

    @Query("SELECT item_name FROM FridgeItem")
    fun getFridgeItemsNameList(): List<String>

    @Update
    fun update(fridgeItem: FridgeItem)

    @Query("UPDATE FridgeItem SET item_name = :name WHERE id = :id")
    fun updateName(id: Int, name: String)

    @Query("UPDATE FridgeItem SET image_url = :photoUrl WHERE id = :id")
    fun updatePhotoUrl(id: Int, photoUrl: String?)

    @Query("UPDATE FridgeItem SET buying_date = :buyingDate WHERE id = :id")
    fun updateBuyingDate(id: Int, buyingDate: Long)

    @Query("UPDATE FridgeItem SET expiry_date = :expiryDate WHERE id = :id")
    fun updateExpiryDate(id: Int, expiryDate: Long)

    @Query("UPDATE FridgeItem SET amount_measure = :countMeasure WHERE id = :id")
    fun updateCountMeasure(id: Int, countMeasure: String)

    @Query("UPDATE FridgeItem SET category = :category WHERE id = :id")
    fun updateCategory(id: Int, category: String)

    @Query("DELETE FROM FridgeItem")
    fun deleteAll()
}
