package com.example.fridgeapp.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Represents an item stored in my fridge.
 * This class is used to map the item data in the Firebase database.
 */
@IgnoreExtraProperties
class FridgeItem {
    var name: String? = null
    var category: String? = null
    var quantity: Int = 0
    var amountMeasure: String? = null
    var buyingDate: Long = 0
    var expiryDate: Long = 0
    var photoUrl: String? = null

    // Default constructor
    constructor()

    constructor(
        name: String?,
        category: String?,
        quantity: Int,
        amountMeasure: String?,
        buyingDate: Long,
        expiryDate: Long,
        photoUrl: String?
    ) {
        this.name = name
        this.category = category
        this.quantity = quantity
        this.amountMeasure = amountMeasure
        this.buyingDate = buyingDate
        this.expiryDate = expiryDate
        this.photoUrl = photoUrl
    }
}
