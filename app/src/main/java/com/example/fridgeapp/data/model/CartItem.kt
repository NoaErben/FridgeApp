package com.example.fridgeapp.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Represents an item in the shopping cart.
 * This class is used to map the item data in the Firebase database.
 */
@IgnoreExtraProperties
class CartItem {
    var name: String? = null
    var category: String? = null
    var quantity: Int = 0
    var amountMeasure: String? = null
    var addedDate: Long = 0
    var photoUrl: String? = null

    // Default constructor
    constructor()

    constructor(
        name: String?,
        category: String?,
        quantity: Int,
        amountMeasure: String?,
        addedDate: Long,
        photoUrl: String?
    ) {
        this.name = name
        this.category = category
        this.quantity = quantity
        this.amountMeasure = amountMeasure
        this.addedDate = addedDate
        this.photoUrl = photoUrl
    }
}
