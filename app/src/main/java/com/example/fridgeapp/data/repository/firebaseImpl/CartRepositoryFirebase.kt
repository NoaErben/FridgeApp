package com.example.fridgeapp.data.repository.firebaseImpl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.CartRepository
import com.example.fridgeapp.data.ui.utils.MyBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class CartRepositoryFirebase : CartRepository {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val fridgeDatabaseReference =
        FirebaseDatabase.getInstance().getReference("itemsInFridge")
    private val cartDatabaseReference =
        FirebaseDatabase.getInstance().getReference("shoppingCartItems")

    private val storageReference = FirebaseStorage.getInstance().reference

    override fun currentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun getCartItems(): LiveData<List<CartItem>> {
        val data = MutableLiveData<List<CartItem>>()
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            cartDatabaseReference.child(currentUser.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<CartItem>()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(CartItem::class.java)
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

    override fun getFridgeItems(): LiveData<List<FridgeItem>> {
        val data = MutableLiveData<List<FridgeItem>>()
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            fridgeDatabaseReference.child(currentUser.uid).addValueEventListener(object : ValueEventListener {
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


    override fun saveCartItemToDatabase(cartItem: CartItem, imageChanged: Boolean, imageUri: Uri?, context: Context,
                                        onComplete: (Result<Unit>) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        uid?.let {
            cartDatabaseReference.child(it).child(cartItem.name.toString()).setValue(cartItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (imageChanged) {
                            uploadCartItemImage(uid, cartItem, imageUri.toString(), context, onComplete)
                        } else {
                            updateCartDatabaseWithPhotoUrl(uid, cartItem, onComplete)
                        }
                    } else {
                        onComplete(Result.failure(Exception("Failed to add item")))
                    }
                }
        }
    }



    private fun uploadCartItemImage(uid: String, cartItem: CartItem, imageUri: String?, context: Context,
                                      onComplete: (Result<Unit>) -> Unit) {
        if (imageUri != null) {
            val imageUriParsed = Uri.parse(imageUri)
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageUriParsed)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, imageUriParsed)
                    ImageDecoder.decodeBitmap(source)
                }

                val compressedBitmap = MyBitmap.compressBitmap(bitmap, 1024)
                val outputStream = ByteArrayOutputStream()
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val data = outputStream.toByteArray()

                val storageRef = storageReference.child("images/$uid/${System.currentTimeMillis()}.jpg")
                val uploadTask = storageRef.putBytes(data)

                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        cartItem.photoUrl = uri.toString()
                        updateCartDatabaseWithPhotoUrl(uid, cartItem, onComplete)
                    }.addOnFailureListener { exception ->
                        onComplete(Result.failure(Exception("Failed to get download URL", exception)))
                    }
                }.addOnFailureListener { exception ->
                    onComplete(Result.failure(Exception("Failed to upload image", exception)))
                }
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
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

    override fun updateCartItemInDatabase(cartItem: CartItem, context: Context,
                                            photoUri: String?, onComplete: (Result<Unit>) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        uid?.let {
            if (photoUri != null && !photoUri.contains("firebase")) {
                uploadCartItemImage(uid, cartItem, photoUri, context, onComplete)
            } else {
                cartItem.photoUrl = photoUri.toString()
                updateCartDatabaseWithPhotoUrl(uid, cartItem, onComplete)
            }
        } ?: run {
            onComplete(Result.failure(Exception("User not logged in")))
        }
    }

    override fun deleteItemFromCartDatabase(cartItem: CartItem, onComplete: (Result<Unit>) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
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

    override fun deleteAllItemsFromCartDatabase(onComplete: (Result<Unit>) -> Unit) {
        // TODO: use somewhere
        val uid = firebaseAuth.currentUser?.uid
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

    override fun checkCartItemExists(itemName: String, callback: (Boolean) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
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

    override fun deleteItemFromFridgeDatabase(fridgeItem: FridgeItem, onComplete: (Result<Unit>) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
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
}