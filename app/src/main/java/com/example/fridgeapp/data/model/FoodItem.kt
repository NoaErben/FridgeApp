package com.example.fridgeapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FoodItem")
data class FoodItem (
    @ColumnInfo(name = "item_name")
    val name: String,

    @ColumnInfo(name = "image_url")
    val photoUrl: String?, // Nullable in case there is no photo

    @ColumnInfo(name = "days_to_expire")
    val daysToExpire: Int,

    @ColumnInfo(name = "category")
    val category: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
