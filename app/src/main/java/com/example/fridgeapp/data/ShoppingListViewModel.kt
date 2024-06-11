package com.example.fridgeapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fridgeapp.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ShoppingListViewModel : ViewModel() {

    private val _shoppingList = MutableLiveData<List<CartItem>>()
    val shoppingList: LiveData<List<CartItem>> get() = _shoppingList

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("shoppingCartItems")
    private val uid: String? = FirebaseAuth.getInstance().currentUser?.uid

    init {
//        fetchShoppingList()
    }

//    private fun fetchShoppingList() {
//        uid?.let { userId ->
//            databaseReference.child(userId).addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val items = mutableListOf<CartItem>()
//                    for (itemSnapshot in snapshot.children) {
//                        val item = itemSnapshot.getValue(CartItem::class.java)
//                        item?.let { items.add(it) }
//                    }
//                    _shoppingList.value = items
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Handle database error
//                }
//            })
//        }
//    }

}
