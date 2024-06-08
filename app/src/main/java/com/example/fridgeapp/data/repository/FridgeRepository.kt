package com.example.fridgeapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fridgeapp.data.model.FridgeItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FridgeRepository {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val database =
        userId?.let { FirebaseDatabase.getInstance().getReference("itemsInFridge").child(it) }
    fun getItems(): LiveData<List<FridgeItem>> {
        val data = MutableLiveData<List<FridgeItem>>()

        database?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<FridgeItem>()
                Log.d("MyTag", "DataSnapshot children count: ${snapshot.childrenCount}")
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(FridgeItem::class.java)
                    if (item != null) {
                        items.add(item)
                        Log.d("MyTag", "Fetched item: ${item.name}, ${item.expiryDate}, ${item.photoUrl}")
                    } else {
                        Log.d("MyTag", "Item is null for snapshot: ${itemSnapshot.key}")
                    }
                }
                Log.d("MyTag", "Total fetched items: ${items.size}")
                data.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyTag", "Failed to fetch items: ${error.message}")
            }
        })

        return data
    }
}