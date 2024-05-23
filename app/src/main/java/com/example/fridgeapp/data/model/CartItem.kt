package com.example.fridgeapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartItem")
data class CartItem (
    // TODO: think about it
    @ColumnInfo(name = "item_name")
    val name: String,

    @ColumnInfo(name = "amount")
    val count: Int,

    @ColumnInfo(name = "image_url")
    val photoUrl: String?, // Nullable in case there is no photo

    @ColumnInfo(name = "buying_date")
    val buyingDate: Long // Unix timestamp
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
