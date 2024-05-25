package com.example.fridgeapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FridgeItem")
data class FridgeItem (
    @ColumnInfo(name = "item_name")
    val name: String,

    @ColumnInfo(name = "amount")
    val count: Int,

    @ColumnInfo(name = "amount_measure")
    val countMeasure: String,

    @ColumnInfo(name = "image_url")
    val photoUrl: String?, // Nullable in case there is no photo

    @ColumnInfo(name = "buying_date")
    val buyingDate: Long, // Unix timestamp

    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long, // Unix timestamp

    @ColumnInfo(name = "category")
    val category: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
