package com.example.fridgeapp.data.repository.firebaseImpl

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.FridgeRepository
import com.example.fridgeapp.data.ui.utils.MyBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class FridgeRepositoryFirebase : FridgeRepository {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    private val fridgeDatabaseReference =
        FirebaseDatabase.getInstance().getReference("itemsInFridge")

    private val storageReference = FirebaseStorage.getInstance().reference

    override fun currentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun getItems(): LiveData<List<FridgeItem>> {
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


    override fun saveFridgeItemToDatabase(fridgeItem: FridgeItem, imageChanged: Boolean, imageUri: Uri?, context: Context,
                                          onComplete: (Result<Unit>) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        uid?.let {
            fridgeDatabaseReference.child(it).child(fridgeItem.name.toString()).setValue(fridgeItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (imageChanged) {
                            uploadFridgeItemImage(uid, fridgeItem, imageUri.toString(), context, onComplete)
                        } else {
                            updateFridgeDatabaseWithPhotoUrl(uid, fridgeItem, onComplete)
                        }
                    } else {
                        onComplete(Result.failure(Exception("Failed to add item")))
                    }
                }
        }
    }



    private fun uploadFridgeItemImage(uid: String, fridgeItem: FridgeItem, imageUri: String?, context: Context,
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
                        fridgeItem.photoUrl = uri.toString()
                        updateFridgeDatabaseWithPhotoUrl(uid, fridgeItem, onComplete)
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

    override fun updateFridgeItemInDatabase(fridgeItem: FridgeItem, context: Context,
                                            photoUri: String?, onComplete: (Result<Unit>) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
        uid?.let {
            if (photoUri != null && !photoUri.contains("firebase")) {
                uploadFridgeItemImage(uid, fridgeItem, photoUri, context, onComplete)
            } else {
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

    override fun deleteAllItemsFromFridgeDatabase(onComplete: (Result<Unit>) -> Unit) {
        // TODO: use somewhere
        val uid = firebaseAuth.currentUser?.uid
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

    override fun checkItemExists(itemName: String, callback: (Boolean) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid
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


}