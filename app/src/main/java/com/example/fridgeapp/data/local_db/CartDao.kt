package com.example.fridgeapp.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fridgeapp.data.model.CartItem

@Dao
interface CartDao {

    @Insert
    fun insert(cartItem: CartItem)

    @Delete
    fun delete(cartItem: CartItem)

    @Query("DELETE FROM CartItem WHERE item_name = :name")
    fun deleteByName(name: String)

    @Query("SELECT * FROM CartItem")
    fun getAllCartItems(): LiveData<List<CartItem>>

    @Query("SELECT * FROM CartItem WHERE id = :id")
    fun getCartItemById(id: Int): CartItem?

    @Query("SELECT * FROM CartItem WHERE item_name = :name")
    fun getCartItemByName(name: String): CartItem?

    @Query("SELECT item_name FROM CartItem")
    fun getCartItemsNameList(): LiveData<List<String>>

    @Update
    fun update(cartItem: CartItem)

    @Query("UPDATE CartItem SET item_name = :name WHERE id = :id")
    fun updateName(id: Int, name: String)

    @Query("UPDATE CartItem SET amount = :count WHERE id = :id")
    fun updateCount(id: Int, count: Int)

    @Query("UPDATE CartItem SET amount_measure = :countMeasure WHERE id = :id")
    fun updateCountMeasure(id: Int, countMeasure: String)

    @Query("UPDATE CartItem SET image_url = :photoUrl WHERE id = :id")
    fun updateImageUrl(id: Int, photoUrl: String?)

    @Query("UPDATE CartItem SET category = :category WHERE id = :id")
    fun updateCategory(id: Int, category: String)

    @Query("UPDATE CartItem SET remarks = :remarks WHERE id = :id")
    fun updateRemarks(id: Int, remarks: String)

    @Query("DELETE FROM CartItem")
    fun deleteAll()
}
