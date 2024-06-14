package com.example.fridgeapp.data.ui.shoppingList

import android.R
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.data.ui.utils.CustomArrayAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.data.ui.viewModels.RoomViewModel
import com.example.fridgeapp.databinding.ShoppingEditItemBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditItemShoppingListFragment: Fragment(), DatePickerDialog.OnDateSetListener {

    private var _binding: ShoppingEditItemBinding? = null
    private val binding get() = _binding!!

    private val roomViewModel: RoomViewModel by activityViewModels()
    private val fbViewModel: FbViewModel by activityViewModels()

    private var imageUri: Uri? = null
    private var imageUriStr: String? = null
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
        _binding = ShoppingEditItemBinding.inflate(inflater, container, false)

        binding.imageView.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupMeasureSpinner()
        observeChosenCartItem()
        setupSaveButton()
        setupThrowOutButton()
        setupImagePicker()
        handleBackPressed()
    }

    private fun observeChosenCartItem() {
        fbViewModel.chosenCartItem.observe(viewLifecycleOwner) { item ->
            binding.itemName.text = item.name
            setCategorySpinnerSelection(item)
            binding.quantity.setText(item.quantity.toString())
            setMeasureSpinnerSelection(item)
            loadImage(item.photoUrl)
            binding.dateData.text = fbViewModel.formatLongDateToString(item.addedDate)
        }
    }

    private fun setCategorySpinnerSelection(item: CartItem) {
        val defaultCategoryIndex = roomViewModel.categories.indexOf(item.category)
        binding.productCategory.setSelection(defaultCategoryIndex)
    }

    private fun setMeasureSpinnerSelection(item: CartItem) {
        val defaultMeasureIndex = roomViewModel.unitMeasures.indexOf(item.amountMeasure)
        binding.measureCategory.setSelection(defaultMeasureIndex)
    }

    private fun loadImage(photoUrl: String?) {
        // Load the image URI using Glide
        Glide.with(requireContext())
            .load(photoUrl)
            .error(com.example.fridgeapp.R.drawable.new_food_option_2) // Placeholder in case of error
            .into(binding.imageView)
        if (photoUrl != null && photoUrl.toString() != "null"){
//            Log.d("EFIF", photoUrl)
            imageUriStr = photoUrl
        }
    }

    private fun setupCategorySpinner() {
        val categories = roomViewModel.categories
        val adapter = CustomArrayAdapter(
            requireContext(), R.layout.simple_spinner_item, categories,
            com.example.fridgeapp.R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter
    }

    private fun setupMeasureSpinner() {
        val categories = roomViewModel.unitMeasures
        val adapter = CustomArrayAdapter(
            requireContext(), R.layout.simple_spinner_item, categories,
            com.example.fridgeapp.R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.measureCategory.adapter = adapter
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
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()

//        Log.d("EFIF", productName + ", " + quantity + ", " + buyingDate + ", " + expiryDate + ", " + productCategory + ", " + amountMeasure + ", " )
//        Log.d("EFIF", viewModel.chosenFridgeItem.value!!.name.toString() + ", " + viewModel.chosenFridgeItem.value!!.quantity.toString() + ", " + convertTimestampToDateString(viewModel.chosenFridgeItem.value!!.buyingDate) + ", " + convertTimestampToDateString(viewModel.chosenFridgeItem.value!!.expiryDate) + ", " + viewModel.chosenFridgeItem.value!!.category.toString() + ", " + viewModel.chosenFridgeItem.value!!.amountMeasure.toString() + ", " )

        return productName != fbViewModel.chosenCartItem.value!!.name.toString()  ||
                quantity != fbViewModel.chosenCartItem.value!!.quantity.toString() ||
                productCategory != fbViewModel.chosenCartItem.value!!.category.toString() ||
                amountMeasure != fbViewModel.chosenCartItem.value!!.amountMeasure.toString() ||
                imageChanged
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(calendar.time)
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
                fbViewModel.chosenCartItem.value?.let { item ->
                    fbViewModel.deleteItemFromCartDatabase(
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
            if (validateInput()) {
                showProgressBar()
                updateCartItem()
            }
        }
    }

    private fun updateCartItem() {
        val productName = fbViewModel.chosenCartItem.value!!.name.toString()
        val quantity = binding.quantity.text.toString().toIntOrNull() ?: 0
        val productCategory = binding.productCategory.selectedItem.toString()
        val amountMeasure = binding.measureCategory.selectedItem.toString()

        fbViewModel.updateCartItemInDatabase(
            productName,
            quantity,
            productCategory,
            amountMeasure,
            fbViewModel.parseDate(binding.dateData.text.toString()),
            imageUriStr,
        ) { result ->
            result.onSuccess {
                hideProgressBar()
                showToast("Added successfully")
                findNavController().navigate(com.example.fridgeapp.R.id.action_editItemShoppingListFragment_to_fridgeShoppingListFragment)
            }.onFailure { exception ->
                hideProgressBar()
                showToast("Failed to add item: ${exception.message}")
            }
        }
    }

    private fun validateInput(): Boolean {
        val quantity = binding.quantity.text.toString()

        return when {
            quantity.isBlank() -> {
                showToast(getString(com.example.fridgeapp.R.string.please_enter_a_valid_quantity))
                false
            }
            else -> true
        }
    }

    private fun navigateToMainFrag() {
        findNavController().navigate(com.example.fridgeapp.R.id.action_editItemShoppingListFragment_to_fridgeShoppingListFragment)
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
