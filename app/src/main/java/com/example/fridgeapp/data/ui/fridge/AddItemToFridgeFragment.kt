package com.example.fridgeapp.data.ui.fridge

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.data.ui.utils.CustomArrayAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.databinding.FridgeAddItemBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.request.transition.Transition


class AddItemToFridgeFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var _binding: FridgeAddItemBinding? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var dialog: Dialog
    private var imageUri: Uri? = null
    private var currentImage: String? = null
    private var isBuyingDate: Boolean = false

    private val binding get() = _binding!!
    private val viewModel: FridgeViewModel by activityViewModels()


    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                handleImageSelection(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FridgeAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFirebase()
        setupCategorySpinner()
        setupMeasureSpinner()
        setupNameSpinner()
        setupDatePickers()
        setupAddItemButton()
        setupImagePicker()
        handleBackPressed()
    }

    private fun initializeFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("itemsInFridge")
        storageReference = FirebaseStorage.getInstance().reference
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

    private fun setupMeasureSpinner() {
        val categories = viewModel.unitMeasures
        val adapter = CustomArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, categories,
            R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.measureCategory.adapter = adapter
    }

    private fun setupNameSpinner() {
        var foodItemsNames = viewModel.foodItemsNames?.value?.toMutableList()
        if (foodItemsNames != null) {
            foodItemsNames.add(0, "")
            foodItemsNames.add("Other")
        } else {
            foodItemsNames = arrayOf("Other").toMutableList()
        }

        val adapter = CustomArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, foodItemsNames,
            R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productName.adapter = adapter

        binding.productName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedName = foodItemsNames[position]
                when (selectedName) {
                    "" -> {
                        Glide.with(requireContext())
                            .load(R.drawable.dish)
                            .into(binding.itemImage)
                        currentImage = R.drawable.dish.toString()
                    }
                    "Other" -> {
                        Dialogs.showCustomProductNameDialog(requireContext(), binding.productName, binding.productName.adapter as ArrayAdapter<String>)
                    }
                    else -> {
                        val foodItem = viewModel.getFoodItem(selectedName)
                        foodItem?.let {
                            binding.productCategory.setSelection(viewModel.categories.indexOf(it.category ?: viewModel.categories[0]))
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DATE, it.daysToExpire ?: 7)
                            val expiryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                            binding.productDaysToExpire.setText(expiryDate)
                            // Load image with Glide
                            Glide.with(requireContext())
                                .load(it.photoUrl)
                                .placeholder(R.drawable.dish) // Placeholder while loading
                                .error(R.drawable.dish) // Placeholder in case of error
                                .into(binding.itemImage)
                            imageUri = it.photoUrl?.toUri()
                            currentImage = it.photoUrl
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }


    private fun setupDatePickers() {
        // Initialize buyingDate to today
        val today = Calendar.getInstance().time
        binding.buyingDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today))

        // Initialize expiryDate to a week ahead
        val weekAhead = Calendar.getInstance().apply { add(Calendar.DATE, 7) }.time
        binding.productDaysToExpire.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(weekAhead))

        binding.buyingDate.setOnClickListener {
            isBuyingDate = true
            showDatePickerDialog()
        }

        binding.productDaysToExpire.setOnClickListener {
            isBuyingDate = false
            showDatePickerDialog()
        }
    }

    private fun setupAddItemButton() {
        binding.addItemButton.setOnClickListener {
            if (!viewModel.isUserLoggedIn()){
                showToast(getString(R.string.please_login_to_add_items))
            }
            if (validateInput()) {
                showProgressBar()
                saveItemToDatabase()
            }
        }
    }

    private fun validateInput(): Boolean {
        val productName = binding.productName.tag as? String ?: binding.productName.selectedItem?.toString() ?: ""
        val quantity = binding.quantity.text.toString()
        val buyingDate = binding.buyingDate.text.toString()
        val expiringDate = binding.productDaysToExpire.text.toString()

        return when {
            productName.length < 2 -> {
                showToast(getString(R.string.please_enter_a_valid_product_name))
                false
            }

            quantity.isBlank() -> {
                showToast(getString(R.string.please_enter_a_valid_quantity))
                false
            }

            buyingDate.isBlank() || expiringDate.isBlank() -> {
                showToast(getString(R.string.please_enter_valid_dates))
                false
            }

            parseDate(expiringDate) <= parseDate(buyingDate) -> {
                showToast(getString(R.string.expiry_date_must_be_after_buying_date))
                false
            }

            else -> true
        }
    }

    private fun saveItemToDatabase() {
        val productName = binding.productName.tag as? String ?: binding.productName.selectedItem?.toString() ?: ""
        val quantity = binding.quantity.text.toString().toIntOrNull() ?: 0
        val buyingDate = parseDate(binding.buyingDate.text.toString())
        val expiryDate = parseDate(binding.productDaysToExpire.text.toString())
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()
        val photoUrl = imageUri.toString()
        val imageChanged = !(currentImage!= null && currentImage!!.contains("firebase")) && currentImage != R.drawable.dish.toString()

        val fridgeItem = FridgeItem(
            name = productName,
            quantity = quantity,
            amountMeasure = amountMeasure,
            photoUrl = photoUrl,
            buyingDate = buyingDate,
            expiryDate = expiryDate,
            category = productCategory
        )


        val uid = viewModel.currentUser.value?.uid
        uid?.let {
            databaseReference.child(it).child(productName).setValue(fridgeItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Only upload the image if it has been changed
                        if (imageChanged) {
                            uploadItemImage(it, fridgeItem)
                        } else {
                            updateDatabaseWithPhotoUrl(uid, fridgeItem)
                        }
                    } else {
                        hideProgressBar()
                        showToast("Failed to add item")
                    }
                }
        }
    }

    private fun uploadItemImage(uid: String, fridgeItem: FridgeItem) {
        if (imageUri != null) {
            // Load the image with Glide
            Glide.with(requireContext())
                .asBitmap()
                .load(imageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // Compress the bitmap
                        val compressedBitmap = compressBitmap(resource, 1024) // TODO: Adjust size as needed

                        // Convert the compressed bitmap to a byte array
                        val outputStream = ByteArrayOutputStream()
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // TODO: Adjust compression quality as needed
                        val data = outputStream.toByteArray()

                        // Upload the compressed image data
                        val storageRef = storageReference.child("images/$uid/${System.currentTimeMillis()}.jpg")
                        val uploadTask = storageRef.putBytes(data)

                        uploadTask.addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                fridgeItem.photoUrl = uri.toString()
                                updateDatabaseWithPhotoUrl(uid, fridgeItem)
                            }
                        }.addOnFailureListener { exception ->
                            hideProgressBar()
                            Log.e("Upload", "Failed to upload image", exception)
                            showToast("Failed to upload image: ${exception.message}")
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle case where the image load is cleared
                    }
                })
        } else {
            hideProgressBar()
            showToast("No image selected")
        }
    }

    private fun compressBitmap(bitmap: Bitmap, maxSizeKb: Int): Bitmap {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        var quality = 100
        while (byteArrayOutputStream.toByteArray().size / 1024 > maxSizeKb) {
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            quality -= 10
        }
        val compressedBitmap = BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.toByteArray().size)
        byteArrayOutputStream.close()
        return compressedBitmap
    }

    private fun updateDatabaseWithPhotoUrl(uid: String, fridgeItem: FridgeItem) {
        fridgeItem.name?.let {
            databaseReference.child(uid).child(it).setValue(fridgeItem)
                .addOnCompleteListener { task ->
                    hideProgressBar()
                    if (task.isSuccessful) {
                        showToast("Item added successfully")
                        findNavController().navigate(R.id.action_addItemToFridgeFragment_to_fridgeManagerFragment)
                    } else {
                        showToast("Failed to update item with photo URL")
                    }
                }
        }
    }

    private fun setupImagePicker() {
        binding.itemImage.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }
    }

    private fun handleImageSelection(uri: Uri) {
        // Load the image URI using Glide
        Glide.with(requireContext())
            .load(uri)
            .placeholder(R.drawable.dish) // Placeholder while loading
            .error(R.drawable.dish) // Placeholder in case of error
            .into(binding.itemImage)
        requireActivity().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        imageUri = uri
        currentImage = uri.toString()
        Log.d("ImagePicker", "Image selected: $imageUri")
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        if (isBuyingDate) {
            calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(binding.buyingDate.text.toString())!!
        } else {
            calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(binding.productDaysToExpire.text.toString())!!
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), this, year, month, day).show()
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(calendar.time)

        if (isBuyingDate) {
            binding.buyingDate.setText(date)
        } else {
            binding.productDaysToExpire.setText(date)
        }
    }

    private fun parseDate(dateStr: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
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

    private fun handleBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showConfirmDiscardChangesDialog()
                } else {
                    findNavController().popBackStack()
                }
            }
        })
    }

    private fun showConfirmDiscardChangesDialog() {
        Dialogs.showConfirmDiscardChangesDialog(
            requireContext(),
            onConfirm = { findNavController().popBackStack() },
            onCancel = { /* Do nothing */ }
        )
    }

    private fun hasUnsavedChanges(): Boolean {
        val defaultCategory = viewModel.categories[0]
        val defaultMeasure = viewModel.unitMeasures[0]
        val defaultBuyingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        val defaultExpiryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply { add(Calendar.DATE, 7) }.time)

        val productName = binding.productName.tag as? String ?: binding.productName.selectedItem?.toString() ?: ""
        val quantity = binding.quantity.text.toString()
        val buyingDate = binding.buyingDate.text.toString()
        val expiryDate = binding.productDaysToExpire.text.toString()
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()
//        Log.d("AITF", productName + ", " + quantity + ", " + buyingDate + ", " + expiryDate + ", " + productCategory + ", " + amountMeasure + ", " )

        return productName.isNotEmpty() ||
                quantity.isNotEmpty() ||
                buyingDate != defaultBuyingDate ||
                expiryDate != defaultExpiryDate ||
                productCategory != defaultCategory ||
                amountMeasure != defaultMeasure ||
                currentImage != R.drawable.dish.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
