package com.example.fridgeapp

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.data.ui.favoritesItems.CustomArrayAdapter
import com.example.fridgeapp.databinding.AddItemToFridgeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddItemToFridgeFragment : Fragment() {

    private var _binding: AddItemToFridgeBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var dialog: Dialog

    private val binding
        get() = _binding!!

    private val viewModel: FridgeLiveDataViewModel by activityViewModels()

    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                binding.itemImage.setImageURI(it)
                requireActivity().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                imageUri = it
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddItemToFridgeBinding.inflate(inflater, container, false)
        setupCategorySpinner()
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("itemsInFridge")

        // Set up the add button click listener
        binding.addItemButton.setOnClickListener {
            showProgressBar()
            val productName = binding.productName.text.toString()
            val quantity = binding.quantity.text.toString().toIntOrNull() ?: 0
            val amountMeasure = binding.amountMeasure.text.toString()
            val buyingDate = binding.buyingDate.text.toString().toLongOrNull() ?: System.currentTimeMillis()
            val expiryDate = binding.productDaysToExpire.text.toString().toLongOrNull() ?: System.currentTimeMillis()
            val productCategory = binding.productCategory.selectedItem.toString()
            val photoUrl = imageUri.toString()

            val fridgeItem = FridgeItem(
                name = productName,
                quantity = quantity,
                amountMeasure = amountMeasure,
                photoUrl = photoUrl,
                buyingDate = buyingDate,
                expiryDate = expiryDate,
                category = productCategory
            )
            if (uid != null) {
                databaseReference.child(uid).setValue(fridgeItem).addOnCompleteListener {
                    if (it.isSuccessful) {
                        uploadItemToFridge(uid, fridgeItem)
                        viewModel.addItem(fridgeItem)
                    } else {
                        hideProgressBar()
                        Toast.makeText(requireContext(), "Failed to add item", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            findNavController().navigate(R.id.action_addItemToFridgeFragment_to_fridgeManagerFragment)
        }

        // Set up the image picker click listener
        binding.itemImage.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        return binding.root
    }

    private fun setupCategorySpinner() {
        val categories = viewModel.categories
        val adapter = CustomArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, categories,
            R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter
    }

    private fun uploadItemToFridge(uid: String, fridgeItem: FridgeItem) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${uid}/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show()
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    fridgeItem.photoUrl = uri.toString()
                    databaseReference.child(uid).setValue(fridgeItem)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                hideProgressBar()
                                Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                hideProgressBar()
                                Toast.makeText(requireContext(), "Failed to update item with photo URL", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                findNavController().navigate(R.id.action_addItemToFridgeFragment_to_fridgeManagerFragment)
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
