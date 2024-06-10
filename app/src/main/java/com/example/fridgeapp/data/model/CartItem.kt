package com.example.fridgeapp.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class CartItem {
    var name: String? = null
    var category: String? = null
    var quantity: Int = 0
    var addedDate: Long = 0
    var photoUrl: String? = null

    // Default constructor
    constructor()

    constructor(
        name: String?,
        category: String?,
        quantity: Int,
        addedDate: Long,
        photoUrl: String?
    ) {
        this.name = name
        this.category = category
        this.quantity = quantity
        this.addedDate = addedDate
        this.photoUrl = photoUrl
    }
}
