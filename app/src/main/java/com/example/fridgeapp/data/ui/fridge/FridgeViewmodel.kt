package com.example.fridgeapp.data.ui.fridge

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.AuthRepository
import com.example.fridgeapp.data.repository.FridgeRepository
import com.example.fridgeapp.data.ui.authentication.AuthenticationViewmodel
import com.google.firebase.auth.FirebaseUser

class FridgeViewmodel(private val fridgeRep: FridgeRepository) : ViewModel() {

    private val _chosenFridgeItem = MutableLiveData<FridgeItem>()
    val chosenFridgeItem: LiveData<FridgeItem> get() = _chosenFridgeItem

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _items = MutableLiveData<List<FridgeItem>>()
    val items: LiveData<List<FridgeItem>> get() = _items

    init {
        // Observe the current user and fetch items whenever the user changes
        _currentUser.value = fridgeRep.currentUser()
        _currentUser.observeForever { user ->
            userChanged()
        }

        // Fetch initial items
        fetchItems()
    }

    fun userChanged() {
        Log.d("FridgeViewmodel", "User changed detected, fetching items.")
        fetchItems()
    }

    private fun fetchItems() {
        fridgeRep.getItems().observeForever { itemList ->
            Log.d("FridgeViewmodel", "Fetched ${itemList.size} items for the current user.")
            _items.postValue(itemList)
        }
    }


    fun setFridgeChosenItem(fridgeItem: FridgeItem) {
        Log.d("FVM", fridgeItem.name.toString())
        _chosenFridgeItem.value = fridgeItem
    }

    fun saveFridgeItemToDatabase(productName: String, quantity: Int, buyingDate: Long,
                                 expiryDate: Long, productCategory: String, amountMeasure: String,
                                 photoUrl: String, imageChanged: Boolean, imageUri: Uri?, context: Context,
                                 onComplete: (Result<Unit>) -> Unit) {
        val fridgeItem = FridgeItem(
            name = productName,
            quantity = quantity,
            amountMeasure = amountMeasure,
            photoUrl = photoUrl,
            buyingDate = buyingDate,
            expiryDate = expiryDate,
            category = productCategory
        )

        fridgeRep.saveFridgeItemToDatabase(fridgeItem, imageChanged, imageUri, context){ result ->
            result.onSuccess {
                onComplete(Result.success(Unit))
            }
            result.onFailure {
                onComplete(Result.failure(it))
            }
        }
    }


    fun updateFridgeItemInDatabase(productName: String?, quantity: Int, buyingDate: Long,
                                   expiryDate: Long, productCategory: String?, amountMeasure: String?,
                                   photoUri: String?, context: Context, onComplete: (Result<Unit>) -> Unit) {
        val fridgeItem = FridgeItem(
            name = productName,
            category = productCategory,
            quantity = quantity,
            amountMeasure = amountMeasure,
            buyingDate = buyingDate,
            expiryDate = expiryDate,
            photoUrl = photoUri.toString()
        )

        fridgeRep.updateFridgeItemInDatabase(fridgeItem, context, photoUri){ result ->
            result.onSuccess {
                onComplete(Result.success(Unit))
            }
            result.onFailure {
                onComplete(Result.failure(it))
            }
        }
    }

    fun deleteItemFromFridgeDatabase(fridgeItem: FridgeItem, onComplete: (Result<Unit>) -> Unit) {
        fridgeRep.deleteItemFromFridgeDatabase(fridgeItem){ result ->
            result.onSuccess {
                onComplete(Result.success(Unit))
            }
            result.onFailure {
                onComplete(Result.failure(it))
            }
        }
    }

    fun checkItemExists(itemName: String, callback: (Boolean) -> Unit) {
        fridgeRep.checkItemExists(itemName) { exists ->
            callback(exists)
        }
    }



    class FridgeViewmodelFactory(private val repo: FridgeRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FridgeViewmodel(repo) as T
        }
    }
}