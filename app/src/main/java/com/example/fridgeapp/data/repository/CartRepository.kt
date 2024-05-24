package com.example.fridgeapp.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.local_db.CartDao
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.model.CartItem

class CartRepository(application: Application) {

    private var cartItemDao: CartDao?

    init {
        val db = FridgeDB.getDatabase(application.applicationContext)
        cartItemDao = db?.cartDao()
    }

}
