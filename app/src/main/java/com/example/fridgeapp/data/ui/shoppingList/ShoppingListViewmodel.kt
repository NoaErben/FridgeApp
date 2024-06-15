package com.example.fridgeapp.data.ui.shoppingList

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.CartRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class ShoppingListViewmodel(private val cartRep: CartRepository):  ViewModel() {

    private val _chosenCartItem = MutableLiveData<CartItem>()
    val chosenCartItem: LiveData<CartItem> get() = _chosenCartItem
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _fridgeItems = MutableLiveData<List<FridgeItem>>()
    val fridgeItems: LiveData<List<FridgeItem>> get() = _fridgeItems

    init {
        // Observe the current user and fetch items whenever the user changes
        _currentUser.value = cartRep.currentUser()
        _currentUser.observeForever { user ->
            userChanged()
        }

        // Fetch initial items
        fetchItems()
    }

    fun userChanged() {
//        Log.d("FridgeViewmodel", "User changed detected, fetching items.")
        fetchItems()
    }

    private fun fetchItems() {
        cartRep.getCartItems().observeForever { itemList ->
//            Log.d("FridgeViewmodel", "Fetched ${itemList.size} items for the current user.")
            _cartItems.postValue(itemList)
        }
        cartRep.getFridgeItems().observeForever { itemList ->
//            Log.d("FridgeViewmodel", "Fetched ${itemList.size} items for the current user.")
            _fridgeItems.postValue(itemList)
        }
    }


    fun setCartChosenItem(cartItem: CartItem) {
        Log.d("FVM", cartItem.name.toString())
        _chosenCartItem.value = cartItem
    }

    fun saveCartItemToDatabase(productName: String, quantity: Int, productCategory: String, amountMeasure: String,
                               addedDate: Long, photoUrl: String, imageChanged: Boolean, imageUri: Uri?, context: Context,
                                 onComplete: (Result<Unit>) -> Unit) {
        val cartItem = CartItem(
            name = productName,
            category = productCategory,
            quantity = quantity,
            amountMeasure = amountMeasure,
            addedDate = addedDate,
            photoUrl = photoUrl
        )
        viewModelScope.launch {
            cartRep.saveCartItemToDatabase(cartItem, imageChanged, imageUri, context) { result ->
                result.onSuccess {
                    onComplete(Result.success(Unit))
                }
                result.onFailure {
                    onComplete(Result.failure(it))
                }
            }
        }
    }


    fun updateCartItemInDatabase(productName: String, quantity: Int, productCategory: String, amountMeasure: String,
                                 addedDate: Long, photoUrl: String?, context: Context, onComplete: (Result<Unit>) -> Unit) {
        val cartItem = CartItem(
            name = productName,
            category = productCategory,
            quantity = quantity,
            amountMeasure = amountMeasure,
            addedDate = addedDate,
            photoUrl = photoUrl
        )
        viewModelScope.launch {
            cartRep.updateCartItemInDatabase(cartItem, context, photoUrl) { result ->
                result.onSuccess {
                    onComplete(Result.success(Unit))
                }
                result.onFailure {
                    onComplete(Result.failure(it))
                }
            }
        }
    }

    fun deleteItemFromCartDatabase(cartItem: CartItem, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            cartRep.deleteItemFromCartDatabase(cartItem) { result ->
                result.onSuccess {
                    onComplete(Result.success(Unit))
                }
                result.onFailure {
                    onComplete(Result.failure(it))
                }
            }
        }
    }

    fun checkCartItemExists(itemName: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            cartRep.checkCartItemExists(itemName) { exists ->
                callback(exists)
            }
        }
    }

    fun deleteItemFromFridgeDatabase(fridgeItem: FridgeItem, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            cartRep.deleteItemFromFridgeDatabase(fridgeItem) { result ->
                result.onSuccess {
                    onComplete(Result.success(Unit))
                }
                result.onFailure {
                    onComplete(Result.failure(it))
                }
            }
        }
    }



    class ShoppingListViewmodelFactory(private val repo: CartRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShoppingListViewmodel(repo) as T
        }
    }
}