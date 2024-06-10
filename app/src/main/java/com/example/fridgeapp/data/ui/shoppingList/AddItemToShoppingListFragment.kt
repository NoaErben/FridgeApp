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
import com.google.firebase.auth.FirebaseAuth
import android.webkit.MimeTypeMap

class AddItemToShoppingListFragment : Fragment() {

    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText
    private lateinit var itemCategorySpinner: Spinner
    private lateinit var itemImageView: ImageView
    private lateinit var addItemButton: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null
    private lateinit var dialog: Dialog
    private lateinit var auth: FirebaseAuth

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
        databaseReference = FirebaseDatabase.getInstance().getReference("shoppingCartItems")
        storageReference = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()

        itemImageView.setOnClickListener {
            openFileChooser()
        }

        addItemButton.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                uploadImageAndSaveItem(uid)
            } else {
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            }
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

    private fun uploadImageAndSaveItem(uid: String) {
        showProgressBar()
        if (selectedImageUri != null) {
            val resolver = requireActivity().contentResolver
            val mimeType = resolver.getType(selectedImageUri!!)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            val fileReference = storageReference.child("uploads/${uid}/${System.currentTimeMillis()}.$extension")
            fileReference.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        saveItem(uid, uri.toString())
                    }
                }
                .addOnFailureListener {
                    hideProgressBar()
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveItem(uid, "")
        }
    }

    private fun saveItem(uid: String, imageUrl: String) {
        val itemName = itemNameEditText.text.toString()
        val itemQuantity = itemQuantityEditText.text.toString().toInt()
        val itemCategory = itemCategorySpinner.selectedItem.toString()
        val addedDate = System.currentTimeMillis()
        val cartItem = CartItem(itemName, itemCategory, itemQuantity, addedDate, imageUrl)

        databaseReference.child(uid).child(itemName).setValue(cartItem)
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
