package com.example.fridgeapp.data.ui.fridge

import android.R
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
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.data.ui.utils.CustomArrayAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.databinding.EditItemInFridgeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditFridgeItemFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var _binding: EditItemInFridgeBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private var imageUriStr: String? = null
    private val viewModel: FridgeViewModel by activityViewModels()

    private var imageChanged = false
    private var isBuyingDate: Boolean = false

    private lateinit var dialog: Dialog


    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(com.example.fridgeapp.R.drawable.dish) // Placeholder while loading
                    .error(com.example.fridgeapp.R.drawable.dish) // Placeholder in case of error
                    .into(binding.imageView)
                requireActivity().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = it
                imageUriStr = it.toString()
                imageChanged = true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = EditItemInFridgeBinding.inflate(inflater, container, false)

        binding.imageView.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupMeasureSpinner()
        setupDatePickers()
        observeChosenFridgeItem()
        setupSaveButton()
        setupThrowOutButton()
        setupImagePicker()
        handleBackPressed()
    }

    private fun observeChosenFridgeItem() {
        viewModel.chosenFridgeItem.observe(viewLifecycleOwner) { item ->
            val currentTime = System.currentTimeMillis()
            var daysUntilExpiry = (item.expiryDate - currentTime) / (1000 * 60 * 60 * 24)
            if ((item.expiryDate - currentTime) >= 0) {
                daysUntilExpiry += 1
            } else {
                daysUntilExpiry
            }

            binding.itemDays.text = "${daysUntilExpiry} days for:"
            if (daysUntilExpiry > 2) {
                binding.itemDays.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_green_dark))
            } else if (daysUntilExpiry > 0) {
                binding.itemDays.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_orange_dark))
            } else {
                binding.itemDays.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_red_dark))
            }

            binding.itemName.text = item.name
            setCategorySpinnerSelection(item)
            binding.quantity.setText(item.quantity.toString())
            setMeasureSpinnerSelection(item)
            loadImage(item.photoUrl)
            binding.buyingDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(item.buyingDate))
            binding.productDaysToExpire.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(item.expiryDate))
        }
    }

    private fun setCategorySpinnerSelection(item: FridgeItem) {
        val defaultCategoryIndex = viewModel.categories.indexOf(item.category)
        binding.productCategory.setSelection(defaultCategoryIndex)
    }

    private fun setMeasureSpinnerSelection(item: FridgeItem) {
        val defaultMeasureIndex = viewModel.unitMeasures.indexOf(item.amountMeasure)
        binding.measureCategory.setSelection(defaultMeasureIndex)
    }

    private fun loadImage(photoUrl: String?) {
        // Load the image URI using Glide
        Glide.with(requireContext())
            .load(photoUrl)
            .error(com.example.fridgeapp.R.drawable.new_food_option_2) // Placeholder in case of error
            .into(binding.imageView)
        if (photoUrl != null && photoUrl.toString() != "null"){
            Log.d("EFIF", photoUrl)
            imageUriStr = photoUrl
        }
    }

    private fun setupCategorySpinner() {
        val categories = viewModel.categories
        val adapter = CustomArrayAdapter(
            requireContext(), R.layout.simple_spinner_item, categories,
            com.example.fridgeapp.R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter
    }

    private fun setupMeasureSpinner() {
        val categories = viewModel.unitMeasures
        val adapter = CustomArrayAdapter(
            requireContext(), R.layout.simple_spinner_item, categories,
            com.example.fridgeapp.R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.measureCategory.adapter = adapter
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

    private fun setupImagePicker() {
        binding.imageView.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }
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
        val productName = binding.itemName.text.toString()
        val quantity = binding.quantity.text.toString()
        val buyingDate = binding.buyingDate.text.toString()
        val expiryDate = binding.productDaysToExpire.text.toString()
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()

//        Log.d("EFIF", productName + ", " + quantity + ", " + buyingDate + ", " + expiryDate + ", " + productCategory + ", " + amountMeasure + ", " )
//        Log.d("EFIF", viewModel.chosenFridgeItem.value!!.name.toString() + ", " + viewModel.chosenFridgeItem.value!!.quantity.toString() + ", " + convertTimestampToDateString(viewModel.chosenFridgeItem.value!!.buyingDate) + ", " + convertTimestampToDateString(viewModel.chosenFridgeItem.value!!.expiryDate) + ", " + viewModel.chosenFridgeItem.value!!.category.toString() + ", " + viewModel.chosenFridgeItem.value!!.amountMeasure.toString() + ", " )

        return productName != viewModel.chosenFridgeItem.value!!.name.toString()  ||
                quantity != viewModel.chosenFridgeItem.value!!.quantity.toString() ||
                buyingDate != convertTimestampToDateString(viewModel.chosenFridgeItem.value!!.buyingDate) ||
                expiryDate != convertTimestampToDateString(viewModel.chosenFridgeItem.value!!.expiryDate) ||
                productCategory != viewModel.chosenFridgeItem.value!!.category.toString() ||
                amountMeasure != viewModel.chosenFridgeItem.value!!.amountMeasure.toString() ||
                imageChanged
    }

    fun convertTimestampToDateString(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(date)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    private fun setupThrowOutButton() {
        binding.throwOutButton.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        Dialogs.showConfirmDeleteDialog(
            requireContext(),
            onConfirm = {
                viewModel.chosenFridgeItem.value?.let { item ->
                    viewModel.deleteItemFromFridgeDatabase(
                        item,
                        onComplete = {})
                    navigateToMainFrag()
                }
            },
            onCancel = { /* Do nothing */ }
        )
    }

    private fun setupSaveButton() {
        binding.useUpButton.setOnClickListener {
            if (!viewModel.isUserLoggedIn()){
                showToast(getString(com.example.fridgeapp.R.string.please_login_to_add_items))
            }
            if (validateInput()) {
                showProgressBar()
                updateFridgeItem()
            }
        }
    }

    private fun updateFridgeItem() {
        val productName = viewModel.chosenFridgeItem.value!!.name.toString()
        val quantity = binding.quantity.text.toString().toIntOrNull() ?: 0
        val buyingDate = viewModel.parseDate(binding.buyingDate.text.toString())
        val expiryDate = viewModel.parseDate(binding.productDaysToExpire.text.toString())
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()

        viewModel.updateFridgeItemInDatabase(
            productName,
            quantity,
            buyingDate,
            expiryDate,
            productCategory,
            amountMeasure,
            imageUriStr,
        ) { result ->
            result.onSuccess {
                hideProgressBar()
                showToast("Added successfully")
                findNavController().navigate(com.example.fridgeapp.R.id.action_editFridgeItemFragment_to_fridgeManagerFragment)
            }.onFailure { exception ->
                hideProgressBar()
                showToast("Failed to add item: ${exception.message}")
            }
        }
    }

    private fun validateInput(): Boolean {
        val quantity = binding.quantity.text.toString()
        val buyingDate = binding.buyingDate.text.toString()
        val expiringDate = binding.productDaysToExpire.text.toString()

        return when {
            quantity.isBlank() -> {
                showToast(getString(com.example.fridgeapp.R.string.please_enter_a_valid_quantity))
                false
            }

            buyingDate.isBlank() || expiringDate.isBlank() -> {
                showToast(getString(com.example.fridgeapp.R.string.please_enter_valid_dates))
                false
            }

            viewModel.parseDate(expiringDate) < viewModel.parseDate(buyingDate) -> {
                showToast(getString(com.example.fridgeapp.R.string.expiry_date_must_be_after_buying_date))
                false
            }

            else -> true
        }
    }

    private fun navigateToMainFrag() {
        findNavController().navigate(com.example.fridgeapp.R.id.action_editFridgeItemFragment_to_fridgeManagerFragment)
    }

    private fun showProgressBar() {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(com.example.fridgeapp.R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
