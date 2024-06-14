package com.example.fridgeapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fridgeapp.data.model.FridgeItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FridgeRepositoryOld {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val database by lazy {
        FirebaseDatabase.getInstance().getReference("itemsInFridge")
    }
    fun getItems(): LiveData<List<FridgeItem>> {
        val data = MutableLiveData<List<FridgeItem>>()
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            database.child(currentUser.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<FridgeItem>()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(FridgeItem::class.java)
                        if (item != null) {
                            items.add(item)
                        }
                    }
                    data.value = items
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
        } else {
            data.value = emptyList()
        }

        return data
    }
}