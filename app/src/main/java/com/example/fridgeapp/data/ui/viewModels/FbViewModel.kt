package com.example.fridgeapp.data.ui.viewModels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.CartRepository
import com.example.fridgeapp.data.repository.FridgeRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FbViewModel (application: Application) : AndroidViewModel(application){

    private val fridgeDatabaseReference =
        FirebaseDatabase.getInstance().getReference("itemsInFridge")
    private val cartDatabaseReference =
        FirebaseDatabase.getInstance().getReference("shoppingCartItems")
    private val storageReference = FirebaseStorage.getInstance().reference
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    private val _chosenFridgeItem = MutableLiveData<FridgeItem>()
    private val _chosenCartItem = MutableLiveData<CartItem>()

    private val fridgeRepository = FridgeRepository()
    val items: LiveData<List<FridgeItem>> = fridgeRepository.getItems()

    private val cartRepository = CartRepository()
    val cartItems: LiveData<List<CartItem>> = cartRepository.getItems()


    val currentUser: LiveData<FirebaseUser?> get() = _currentUser
    val chosenFridgeItem: LiveData<FridgeItem> get() = _chosenFridgeItem
    val chosenCartItem: LiveData<CartItem> get() = _chosenCartItem

    // ################## VM functions ##################
    init {
        // Check if the user is already logged in
        _currentUser.value = auth.currentUser
        items.observeForever { itemList ->
            Log.d("MyTag", "Items fetched in ViewModel: ${itemList.size}")
            itemList.forEach {
                Log.d("MyTag", "Item: ${it.name}, Expiry: ${it.expiryDate}, Photo: ${it.photoUrl}")
            }
        }

        cartItems.observeForever { itemList ->
            Log.d("MyTag", "Items fetched in ViewModel: ${itemList.size}")
            itemList.forEach {
//                Log.d("MyTag", "Item: ${it.name}, Expiry: ${it.expiryDate}, Photo: ${it.photoUrl}")
            }
        }
    }

    fun setFridgeChosenItem(fridgeItem: FridgeItem) {
        _chosenFridgeItem.value = fridgeItem
    }

    fun setCartChosenItem(cartItem: CartItem) {
        _chosenCartItem.value = cartItem
    }

    private fun setCurrentUser(user: FirebaseUser?) {
        _currentUser.value = user
    }

    fun parseDate(dateStr: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun formatLongDateToString(dateInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = Date(dateInMillis)
        return dateFormat.format(date)
    }

    // ################## FB authentication functions ##################

    fun signIn(email: String, password: String,
               onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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

    fun signUp(email: String, password: String, name: String,
               onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit,
                               onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String,
                       onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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

    // ################## FB fridge item functions ##################

    fun compressBitmap(bitmap: Bitmap, maxSizeKb: Int): Bitmap {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        var quality = 100
        while (byteArrayOutputStream.toByteArray().size / 1024 > maxSizeKb) {
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            quality -= 10
        }
        val compressedBitmap = BitmapFactory.decodeByteArray(
            byteArrayOutputStream.toByteArray(),
            0,
            byteArrayOutputStream.toByteArray().size
        )
        byteArrayOutputStream.close()
        return compressedBitmap
    }

    fun saveFridgeItemToDatabase(productName: String, quantity: Int, buyingDate: Long,
                                 expiryDate: Long, productCategory: String, amountMeasure: String,
                                 photoUrl: String, imageChanged: Boolean, imageUri: Uri?,
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

        val uid = currentUser.value?.uid
        uid?.let {
            fridgeDatabaseReference.child(it).child(productName).setValue(fridgeItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (imageChanged) {
                            uploadFridgeItemImage(uid, fridgeItem, imageUri.toString(), onComplete)
                        } else {
                            updateFridgeDatabaseWithPhotoUrl(uid, fridgeItem, onComplete)
                        }
                    } else {
                        onComplete(Result.failure(Exception("Failed to add item")))
                    }
                }
        }
    }

    private fun uploadFridgeItemImage(uid: String, fridgeItem: FridgeItem, imageUri: String?,
                                      onComplete: (Result<Unit>) -> Unit) {
        if (imageUri != null) {
            Glide.with(getApplication<Application>().applicationContext)
                .asBitmap()
                .load(imageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val compressedBitmap = compressBitmap(resource, 1024)
                        val outputStream = ByteArrayOutputStream()
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val data = outputStream.toByteArray()

                        val storageRef =
                            storageReference.child("images/$uid/${System.currentTimeMillis()}.jpg")
                        val uploadTask = storageRef.putBytes(data)

                        uploadTask.addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                fridgeItem.photoUrl = uri.toString()
                                updateFridgeDatabaseWithPhotoUrl(uid, fridgeItem, onComplete)
                            }.addOnFailureListener { exception ->
                                onComplete(
                                    Result.failure(
                                        Exception(
                                            "Failed to get download URL",
                                            exception
                                        )
                                    )
                                )
                            }
                        }.addOnFailureListener { exception ->
                            onComplete(
                                Result.failure(
                                    Exception(
                                        "Failed to upload image",
                                        exception
                                    )
                                )
                            )
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            onComplete(Result.failure(Exception("No image selected")))
        }
    }

    private fun updateFridgeDatabaseWithPhotoUrl(uid: String, fridgeItem: FridgeItem,
                                                 onComplete: (Result<Unit>) -> Unit) {
        fridgeItem.name?.let {
            fridgeDatabaseReference.child(uid).child(it).setValue(fridgeItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(Result.success(Unit))
                    } else {
                        onComplete(Result.failure(Exception("Failed to update photo URL")))
                    }
                }
        }
    }

    fun updateFridgeItemInDatabase(productName: String?, quantity: Int, buyingDate: Long,
                                   expiryDate: Long, productCategory: String?, amountMeasure: String?,
                                   photoUri: String?, onComplete: (Result<Unit>) -> Unit) {
        val fridgeItem = FridgeItem(
            name = productName,
            category = productCategory,
            quantity = quantity,
            amountMeasure = amountMeasure,
            buyingDate = buyingDate,
            expiryDate = expiryDate,
            photoUrl = photoUri.toString()
        )

        val uid = currentUser.value?.uid
        uid?.let {
            if (photoUri != null && !photoUri.contains("firebase")) {
                Log.d("FVM", "doesnt contain FB")
                uploadFridgeItemImage(uid, fridgeItem, photoUri, onComplete)
            } else {
                Log.d("FVM", chosenFridgeItem.value?.photoUrl.toString())
                fridgeItem.photoUrl = photoUri.toString()
                updateFridgeDatabaseWithPhotoUrl(uid, fridgeItem, onComplete)
            }
        } ?: run {
            onComplete(Result.failure(Exception("User not logged in")))
        }
    }

    private fun updateFridgeDatabaseItem(uid: String, fridgeItem: FridgeItem,
                                         onComplete: (Result<Unit>) -> Unit) {
        fridgeItem.name?.let {
            fridgeDatabaseReference.child(uid).child(it).setValue(fridgeItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(Result.success(Unit))
                    } else {
                        onComplete(Result.failure(Exception("Failed to update item")))
                    }
                }
        }
    }

    fun deleteItemFromFridgeDatabase(fridgeItem: FridgeItem, onComplete: (Result<Unit>) -> Unit) {
        val uid = currentUser.value?.uid
        uid?.let {
            fridgeItem.name?.let { it1 ->
                fridgeDatabaseReference.child(it).child(it1).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete(Result.success(Unit))
                        } else {
                            onComplete(Result.failure(Exception("Failed to delete item")))
                        }
                    }
            }
        } ?: run {
            onComplete(Result.failure(Exception("User not logged in")))
        }
    }

    fun deleteAllItemsFromFridgeDatabase(onComplete: (Result<Unit>) -> Unit) {
        // TODO: use somewhere
        val uid = currentUser.value?.uid
        uid?.let {
            fridgeDatabaseReference.child(it).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(Result.success(Unit))
                    } else {
                        onComplete(Result.failure(Exception("Failed to delete all items")))
                    }
                }
        } ?: run {
            onComplete(Result.failure(Exception("User not logged in")))
        }
    }

    fun checkItemExists(itemName: String, callback: (Boolean) -> Unit) {
        val uid = currentUser.value?.uid
        uid?.let {
            fridgeDatabaseReference.child(it).child(itemName).get()
                .addOnSuccessListener { dataSnapshot ->
                    callback(dataSnapshot.exists())
                }
                .addOnFailureListener { exception ->
                    Log.e("FbViewModel", "Error checking item existence", exception)
                    callback(false)
                }
        } ?: run {
            callback(false)
        }
    }

    // ################## FB Cart item functions ##################

    fun saveCartItemToDatabase(productName: String, quantity: Int, productCategory: String, amountMeasure: String,
                                 addedDate: Long, photoUrl: String, imageChanged: Boolean, imageUri: Uri?,
                                 onComplete: (Result<Unit>) -> Unit) {
        val cartItem = CartItem(
            name = productName,
            category = productCategory,
            quantity = quantity,
            amountMeasure = amountMeasure,
            addedDate = addedDate,
            photoUrl = photoUrl
        )

        val uid = currentUser.value?.uid
        uid?.let {
            cartDatabaseReference.child(it).child(productName).setValue(cartItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (imageChanged) {
                            uploadCartItemImage(uid, cartItem, imageUri.toString(), onComplete)
                        } else {
                            updateCartDatabaseWithPhotoUrl(uid, cartItem, onComplete)
                        }
                    } else {
                        onComplete(Result.failure(Exception("Failed to add item")))
                    }
                }
        }
    }

    private fun uploadCartItemImage(uid: String, cartItem: CartItem, imageUri: String?,
                                      onComplete: (Result<Unit>) -> Unit) {
        if (imageUri != null) {
            Glide.with(getApplication<Application>().applicationContext)
                .asBitmap()
                .load(imageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val compressedBitmap = compressBitmap(resource, 1024)
                        val outputStream = ByteArrayOutputStream()
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val data = outputStream.toByteArray()

                        val storageRef =
                            storageReference.child("images/$uid/${System.currentTimeMillis()}.jpg")
                        val uploadTask = storageRef.putBytes(data)

                        uploadTask.addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                cartItem.photoUrl = uri.toString()
                                updateCartDatabaseWithPhotoUrl(uid, cartItem, onComplete)
                            }.addOnFailureListener { exception ->
                                onComplete(
                                    Result.failure(
                                        Exception(
                                            "Failed to get download URL",
                                            exception
                                        )
                                    )
                                )
                            }
                        }.addOnFailureListener { exception ->
                            onComplete(
                                Result.failure(
                                    Exception(
                                        "Failed to upload image",
                                        exception
                                    )
                                )
                            )
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            onComplete(Result.failure(Exception("No image selected")))
        }
    }

    private fun updateCartDatabaseWithPhotoUrl(uid: String, cartItem: CartItem,
                                                 onComplete: (Result<Unit>) -> Unit) {
        cartItem.name?.let {
            cartDatabaseReference.child(uid).child(it).setValue(cartItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(Result.success(Unit))
                    } else {
                        onComplete(Result.failure(Exception("Failed to update photo URL")))
                    }
                }
        }
    }

    fun updateCartItemInDatabase(
        productName: String, quantity: Int, productCategory: String, amountMeasure: String,
        addedDate: Long, photoUrl: String?, onComplete: (Result<Unit>) -> Unit) {
        val cartItem = CartItem(
            name = productName,
            category = productCategory,
            quantity = quantity,
            amountMeasure = amountMeasure,
            addedDate = addedDate,
            photoUrl = photoUrl
        )

        val uid = currentUser.value?.uid
        uid?.let {
            if (photoUrl != null && !photoUrl.contains("firebase")) {
//                Log.d("FVM", "doesnt contain FB")
                uploadCartItemImage(uid, cartItem, photoUrl, onComplete)
            } else {
//                Log.d("FVM", chosenFridgeItem.value?.photoUrl.toString())
                cartItem.photoUrl = photoUrl.toString()
                updateCartDatabaseWithPhotoUrl(uid, cartItem, onComplete)
            }
        } ?: run {
            onComplete(Result.failure(Exception("User not logged in")))
        }
    }

    private fun updateCartDatabaseItem(uid: String, cartItem: CartItem,
                                         onComplete: (Result<Unit>) -> Unit) {
        cartItem.name?.let {
            cartDatabaseReference.child(uid).child(it).setValue(cartItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(Result.success(Unit))
                    } else {
                        onComplete(Result.failure(Exception("Failed to update item")))
                    }
                }
        }
    }

    fun deleteItemFromCartDatabase(cartItem: CartItem, onComplete: (Result<Unit>) -> Unit) {
        val uid = currentUser.value?.uid
        uid?.let {
            cartItem.name?.let { it1 ->
                cartDatabaseReference.child(it).child(it1).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete(Result.success(Unit))
                        } else {
                            onComplete(Result.failure(Exception("Failed to delete item")))
                        }
                    }
            }
        } ?: run {
            onComplete(Result.failure(Exception("User not logged in")))
        }
    }

    fun deleteAllItemsFromCartDatabase(onComplete: (Result<Unit>) -> Unit) {
        // TODO: use somewhere
        val uid = currentUser.value?.uid
        uid?.let {
            cartDatabaseReference.child(it).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(Result.success(Unit))
                    } else {
                        onComplete(Result.failure(Exception("Failed to delete all items")))
                    }
                }
        } ?: run {
            onComplete(Result.failure(Exception("User not logged in")))
        }
    }

    fun checkCartItemExists(itemName: String, callback: (Boolean) -> Unit) {
        val uid = currentUser.value?.uid
        uid?.let {
            cartDatabaseReference.child(it).child(itemName).get()
                .addOnSuccessListener { dataSnapshot ->
                    callback(dataSnapshot.exists())
                }
                .addOnFailureListener { exception ->
                    Log.e("FbViewModel", "Error checking item existence", exception)
                    callback(false)
                }
        } ?: run {
            callback(false)
        }
    }


}