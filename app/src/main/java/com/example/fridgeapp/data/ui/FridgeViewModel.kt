package com.example.fridgeapp.data.ui

import FoodRepository
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.model.FridgeItem
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val foodRepository = FoodRepository(application)

    val foodItems: LiveData<List<FoodItem>>? = foodRepository.getAllFoodItems()
    val foodItemsNames: LiveData<List<String>>? = foodRepository.getFoodsNameList()

    val categories = listOf("Breads", "Dairy", "Vegetables", "Meat", "Sauces", "Fish")
    val unitMeasures = listOf("Grams", "Kilograms", "Milliliters", "Liters", "Pieces", "Packets", "Boxes")

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    init {
        // Check if the user is already logged in
        _currentUser.value = auth.currentUser
    }

    // Function to get the concatenated string
    // TODO: delete
    fun getConcatenatedString(): String {
        //foodRepository.deleteAllFoodTable()
        return foodItemsNames?.value?.joinToString(separator = ", ") ?: ""
    }

    private fun setCurrentUser(user: FirebaseUser?) {
        _currentUser.value = user
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                setCurrentUser(auth.currentUser)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        setCurrentUser(null)
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                setCurrentUser(auth.currentUser)
                handleName(name)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            viewModelScope.launch {
                try {
                    user.reauthenticate(credential).await()
                    user.updatePassword(newPassword).await()
                    onSuccess()
                } catch (e: Exception) {
                    onFailure(e)
                }
            }
        } else {
            onFailure(Exception("No authenticated user found"))
        }
    }

    private fun handleName(name: String) {
        // TODO: Handle the additional details as needed
        // For example, save them to a database, update user profile, etc.
    }

    fun getImageResource(context: Context, photoUrl: String): Int {
        return context.resources.getIdentifier(photoUrl, "drawable", context.packageName)
    }

    private val _chosenFridgeItem = MutableLiveData<FridgeItem>()
    val chosenFridgeItem: LiveData<FridgeItem> get() = _chosenFridgeItem

    private val _chosenFoodItem = MutableLiveData<FoodItem>()
    val chosenFoodItem: LiveData<FoodItem> get() = _chosenFoodItem

    private val _chosenCartItem = MutableLiveData<CartItem>()
    val chosenCartItem: LiveData<CartItem> get() = _chosenCartItem


    fun setFoodChosenItem(foodItem: FoodItem) {
        _chosenFoodItem.value = foodItem
    }

    fun setFridgeChosenItem(fridgeItem: FridgeItem) {
        _chosenFridgeItem.value = fridgeItem
    }

    // Methods to use repository functions
    fun insertFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRepository.insert(foodItem)
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRepository.delete(foodItem)
            Log.d("FridgeViewModel", "Food item deleted from repository")
        }
    }

    fun updateFoodItem(foodItem: FoodItem) {
        _chosenFoodItem.value = foodItem
        viewModelScope.launch {
            Log.d("FridgeViewModel", "Updating food item: $foodItem")
            foodRepository.update(foodItem)
            Log.d("FridgeViewModel", "Food item updated in repository")
        }
    }


    fun deleteAllFoodItems() {
        viewModelScope.launch {
            foodRepository.deleteAllFoodTable()
        }
    }

    fun updateFoodName(id: Int, name: String) {
        viewModelScope.launch {
            foodRepository.updateName(id, name)
        }
    }

    fun updateFoodCategory(id: Int, newCategory: String) {
        viewModelScope.launch {
            foodRepository.updateCategory(id, newCategory)
        }

    }

    fun updateFoodPhotoUrl(id: Int, photoUrl: String?) {
        viewModelScope.launch {
            foodRepository.updatePhotoUrl(id, photoUrl)
        }
    }

    fun updateFoodDaysToExpire(id: Int, daysToExpire: Int) {
        viewModelScope.launch {
            foodRepository.updateDaysToExpire(id, daysToExpire)
        }
    }

    fun getFoodItem(name: String): FoodItem? {
        var item: FoodItem? = null
        viewModelScope.launch {
            item = foodRepository.getFoodItem(name)!!
        }
        return item
    }

}