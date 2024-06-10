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
import android.app.Dialog
import android.view.Window
import androidx.navigation.fragment.findNavController


class AddItemToShoppingListFragment : Fragment() {

    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText
    private lateinit var itemCategorySpinner: Spinner
    private lateinit var itemImageView: ImageView
    private lateinit var addItemButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private var selectedImageUri: Uri? = null
    private lateinit var dialog: Dialog

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
        showProgressBar()
        if (selectedImageUri != null) {
            val fileReference = storage.child("uploads/${System.currentTimeMillis()}.jpg")
            fileReference.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        saveItem(uri.toString())
                    }
                }
                .addOnFailureListener {
                    hideProgressBar()
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
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
                hideProgressBar()
                Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show()
                // Navigate to the shopping list fragment
                findNavController().navigate(R.id.action_addItemToShoppingList_to_fridgeShoppingListFragment)
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(requireContext(), "Failed to add item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showProgressBar() {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }
}
