package com.example.fridgeapp.data.ui.fridge

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
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
import com.example.fridgeapp.data.repository.firebaseImpl.FridgeRepositoryFirebase
import com.example.fridgeapp.data.repository.roomImpl.FoodRepositoryRoom
import com.example.fridgeapp.data.ui.favoritesItems.FavoriteViewModel
import com.example.fridgeapp.data.ui.utils.CustomArrayAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.data.ui.utils.MyDates
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.FridgeAddItemBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * AddItemToFridgeFragment is a Fragment class that provides a user interface for adding new items to the fridge.
 * The fragment interacts with a ViewModel to save the new item to a Firebase repository,
 * and it ensures input validation and handles various user interactions,
 */

class AddItemToFridgeFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var binding : FridgeAddItemBinding by autoCleared()

    private val favoriteViewModel: FavoriteViewModel by activityViewModels {
        FavoriteViewModel.FavoriteViewModelFactory(FoodRepositoryRoom(requireActivity().application))
    }

    private lateinit var dialog: Dialog
    private var imageUri: Uri? = null
    private var currentImage: String? = null
    private var isBuyingDate: Boolean = false

    private val viewModel: FridgeViewmodel by activityViewModels {
        FridgeViewmodel.FridgeViewmodelFactory(FridgeRepositoryFirebase())
    }


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
        binding = FridgeAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupMeasureSpinner()
        setupDatePickers()
        setupAddItemButton()
        setupImagePicker()
        handleBackPressed()

        // Observe the LiveData
        favoriteViewModel.foodItemsNames?.observe(viewLifecycleOwner, Observer { foodNames ->
            setupNameSpinner()
        })
    }

    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(com.example.fridgeapp.R.array.categories).toList()
        val adapter = CustomArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, categories,
            R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter
    }

    private fun setupMeasureSpinner() {
        val unitMeasures = resources.getStringArray(com.example.fridgeapp.R.array.unit_measures).toList()
        val adapter = CustomArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, unitMeasures,
            R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.measureCategory.adapter = adapter
    }

    private fun setupNameSpinner() {
        var foodItemsNames = favoriteViewModel.foodItemsNames?.value?.toMutableList()
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
                        currentImage = R.drawable.dish.toString()
                    }
                    "Other" -> {
                        Dialogs.showCustomProductNameDialog(requireContext(), binding.productName, binding.productName.adapter as ArrayAdapter<String>)
                    }
                    else -> {
                        val categories = resources.getStringArray(com.example.fridgeapp.R.array.categories).toList()
                        viewLifecycleOwner.lifecycleScope.launch {
                            val foodItem = favoriteViewModel.getFoodItem(selectedName)
                            foodItem?.let {
                                binding.productCategory.setSelection(categories.indexOf(it.category ?: categories[0]))
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
        val buyingDate = MyDates.parseDate(binding.buyingDate.text.toString())
        val expiryDate = MyDates.parseDate(binding.productDaysToExpire.text.toString())
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()
        val photoUrl = imageUri.toString()
        val imageChanged =
            !(currentImage != null && currentImage!!.contains("firebase")) && currentImage != R.drawable.dish.toString()

        viewModel.saveFridgeItemToDatabase(
            productName,
            quantity,
            buyingDate,
            expiryDate,
            productCategory,
            amountMeasure,
            photoUrl,
            imageChanged,
            imageUri,
            requireContext(),
        ) { result ->
            result.onSuccess {
                hideProgressBar()
                showToast(getString(R.string.added_successfully))
                findNavController().navigate(R.id.action_addItemToFridgeFragment_to_fridgeManagerFragment)
            }.onFailure { exception ->
                hideProgressBar()
                showToast(getString(R.string.failed_to_add_item, exception.message))

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

            MyDates.parseDate(expiringDate) < MyDates.parseDate(buyingDate) -> {
                showToast(getString(R.string.expiry_date_must_be_after_buying_date))
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
        val categories = resources.getStringArray(com.example.fridgeapp.R.array.categories).toList()
        val unitMeasures = resources.getStringArray(com.example.fridgeapp.R.array.unit_measures).toList()
        val defaultCategory = categories[0]
        val defaultMeasure = unitMeasures[0]
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

    private fun checkItemExistsAndSave() {
        val productName = binding.productName.tag as? String ?: binding.productName.selectedItem?.toString() ?: ""

        viewModel.checkItemExists(productName) { exists ->
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
                findNavController().navigate(R.id.action_addItemToFridgeFragment_to_fridgeManagerFragment)
            }
        )
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}