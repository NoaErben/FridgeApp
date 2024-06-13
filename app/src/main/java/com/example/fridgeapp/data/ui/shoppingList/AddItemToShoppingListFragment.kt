package com.example.fridgeapp.data.ui.shoppingList

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.utils.CustomArrayAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.data.ui.viewModels.RoomViewModel
import com.example.fridgeapp.databinding.ShoppingAddItemBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddItemToShoppingListFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var _binding: ShoppingAddItemBinding? = null
    private val binding get() = _binding!!

    private val roomViewModel: RoomViewModel by activityViewModels()
    private val fbViewModel: FbViewModel by activityViewModels()

    private lateinit var dialog: Dialog
    private var imageUri: Uri? = null
    private var currentImage: String? = null
    private var isBuyingDate: Boolean = false


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
        _binding = ShoppingAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupMeasureSpinner()
        setupAddItemButton()
        setupImagePicker()
        handleBackPressed()

        // Observe the LiveData
        roomViewModel.foodItemsNames?.observe(viewLifecycleOwner, Observer { foodNames ->
            setupNameSpinner()
        })
    }

    private fun setupCategorySpinner() {
        val categories = roomViewModel.categories
        val adapter = CustomArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, categories,
            R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter
    }

    private fun setupMeasureSpinner() {
        val categories = roomViewModel.unitMeasures
        val adapter = CustomArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, categories,
            R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.measureCategory.adapter = adapter
    }

    private fun setupNameSpinner() {
        var foodItemsNames = roomViewModel.foodItemsNames?.value?.toMutableList()
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
                        //todo - ask guy
//                        Glide.with(requireContext())
//                            .load(R.drawable.dish)
//                            .into(binding.itemImage)
                        currentImage = R.drawable.dish.toString()
                    }
                    "Other" -> {
                        Dialogs.showCustomProductNameDialog(requireContext(), binding.productName, binding.productName.adapter as ArrayAdapter<String>)
                    }
                    else -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val foodItem = roomViewModel.getFoodItem(selectedName)
                            foodItem?.let {
                                binding.productCategory.setSelection(roomViewModel.categories.indexOf(it.category ?: roomViewModel.categories[0]))
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
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }


    private fun setupAddItemButton() {
        binding.addItemButton.setOnClickListener {
            if (!fbViewModel.isUserLoggedIn()){
                showToast(getString(R.string.please_login_to_add_items))
            }
            if (validateInput()) {
                showProgressBar()
                checkItemExistsAndSave()
            }
        }
    }


    private fun saveItemToDatabase() {
        val productName =
            binding.productName.tag as? String ?: binding.productName.selectedItem?.toString() ?: ""
        val quantity = binding.quantity.text.toString().toIntOrNull() ?: 0
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()
        val photoUrl = imageUri.toString()
        val imageChanged =
            !(currentImage != null && currentImage!!.contains("firebase")) && currentImage != R.drawable.dish.toString()
        val addedDate = System.currentTimeMillis()

        fbViewModel.saveCartItemToDatabase(
            productName,
            quantity,
            productCategory,
            amountMeasure,
            addedDate,
            photoUrl,
            imageChanged,
            imageUri,
        ) { result ->
            result.onSuccess {
                hideProgressBar()
                showToast("Added successfully")
                findNavController().navigate(R.id.action_addItemToShoppingList_to_fridgeShoppingListFragment)
            }.onFailure { exception ->
                hideProgressBar()
                showToast("Failed to add item: ${exception.message}")
            }
        }
    }

    private fun validateInput(): Boolean {
        val productName = binding.productName.tag as? String ?: binding.productName.selectedItem?.toString() ?: ""
        val quantity = binding.quantity.text.toString()

        return when {
            productName.length < 2 -> {
                showToast(getString(R.string.please_enter_a_valid_product_name))
                false
            }

            quantity.isBlank() -> {
                showToast(getString(R.string.please_enter_a_valid_quantity))
                false
            }

            else -> true
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

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
        val defaultCategory = roomViewModel.categories[0]
        val defaultMeasure = roomViewModel.unitMeasures[0]

        val productName = binding.productName.tag as? String ?: binding.productName.selectedItem?.toString() ?: ""
        val quantity = binding.quantity.text.toString()
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()
//        Log.d("AITF", productName + ", " + quantity + ", " + buyingDate + ", " + expiryDate + ", " + productCategory + ", " + amountMeasure + ", " )

        return productName.isNotEmpty() ||
                quantity.isNotEmpty() ||
                productCategory != defaultCategory ||
                amountMeasure != defaultMeasure ||
                currentImage != R.drawable.dish.toString()
    }

    private fun checkItemExistsAndSave() {
        val productName = binding.productName.tag as? String ?: binding.productName.selectedItem?.toString() ?: ""

        fbViewModel.checkCartItemExists(productName) { exists ->
            if (exists) {
                showReplaceDiscardDialog {
                    saveItemToDatabase()
                }
            } else {
                saveItemToDatabase()
            }
        }
    }

    private fun showReplaceDiscardDialog(onReplace: () -> Unit) {
        Dialogs.showReplaceDiscardDialog(
            requireContext(),
            onReplace = {
                onReplace()
            },
            onDiscard = {
                hideProgressBar()
                findNavController().navigate(R.id.action_addItemToShoppingList_to_fridgeShoppingListFragment)
            }
        )
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}