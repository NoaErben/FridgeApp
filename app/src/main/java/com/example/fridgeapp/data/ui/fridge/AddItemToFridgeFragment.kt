package com.example.fridgeapp.data.ui.fridge

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.ui.FridgeLiveDataViewModel
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.favoritesItems.CustomArrayAdapter
import com.example.fridgeapp.databinding.AddItemToFridgeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

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
                // Apply circular crop when setting the image URI
                Glide.with(binding.itemImage.context)
                    .load(imageUri)
                    .circleCrop()
                    .into(binding.itemImage)
                Log.d("ImagePicker", "Image selected: $imageUri")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddItemToFridgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("itemsInFridge")
        storageReference = FirebaseStorage.getInstance().reference

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
                databaseReference.child(uid).child(productName).setValue(fridgeItem).addOnCompleteListener {
                    if (it.isSuccessful) {
                        uploadItemToFridge(uid, fridgeItem)
                    } else {
                        hideProgressBar()
                        Toast.makeText(requireContext(), "Failed to add item", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.itemImage.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }
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
        if (::imageUri.isInitialized) {
            val resolver = requireActivity().contentResolver
            val mimeType = resolver.getType(imageUri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            val storageRef = storageReference.child("images/${uid}/${System.currentTimeMillis()}.$extension")
            Log.d("StoragePath", "Uploading to: ${storageRef.path}")
            Log.d("ImageUri", "Image URI: $imageUri")
            Log.d("MimeType", "MIME Type: $mimeType")
            Log.d("FileExtension", "File Extension: $extension")

            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        fridgeItem.photoUrl = uri.toString()
                        fridgeItem.name?.let {
                            databaseReference.child(uid).child(it).setValue(fridgeItem)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        hideProgressBar()
                                        Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show()
                                        // Navigate to FridgeManagerFragment
                                        findNavController().navigate(R.id.action_addItemToFridgeFragment_to_fridgeManagerFragment)
                                    } else {
                                        hideProgressBar()
                                        Toast.makeText(requireContext(), "Failed to update item with photo URL", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressBar()
                    Log.e("Upload", "Failed to upload image", exception)
                    Toast.makeText(requireContext(), "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            hideProgressBar()
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
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
