package com.example.fridgeapp.data.ui.shoppingList

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.CartItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddItemToShoppingListFragment : Fragment() {

    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText
    private lateinit var itemCategorySpinner: Spinner
    private lateinit var itemImageView: ImageView
    private lateinit var addItemButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private var selectedImageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_item_to_shopping_list, container, false)
        itemNameEditText = view.findViewById(R.id.product_name)
        itemQuantityEditText = view.findViewById(R.id.quantity)
        itemCategorySpinner = view.findViewById(R.id.product_category_spinner)
        itemImageView = view.findViewById(R.id.item_image)
        addItemButton = view.findViewById(R.id.add_item_button)
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference

        itemImageView.setOnClickListener {
            openFileChooser()
        }

        addItemButton.setOnClickListener {
            uploadImageAndSaveItem()
        }

        return view
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            itemImageView.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageAndSaveItem() {
        if (selectedImageUri != null) {
            val fileReference = storage.child("uploads/${System.currentTimeMillis()}.jpg")
            fileReference.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        saveItem(uri.toString())
                    }
                }
                .addOnFailureListener {
                    // Handle failure
                }
        } else {
            saveItem("")
        }
    }

    private fun saveItem(imageUrl: String) {
        val itemName = itemNameEditText.text.toString()
        val itemQuantity = itemQuantityEditText.text.toString().toInt()
        val itemCategory = itemCategorySpinner.selectedItem.toString()
        val addedDate = System.currentTimeMillis()
        val item = CartItem(itemName, itemCategory, itemQuantity, addedDate, imageUrl)
        database.child("shoppingCartItems").push().setValue(item)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}
