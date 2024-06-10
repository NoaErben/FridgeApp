package com.example.fridgeapp.data.ui.favoritesItems

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.data.ui.utils.CustomArrayAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.databinding.FavoriteAddItemBinding

class AddItemToFavoriteFragment : Fragment() {

    private var _binding: FavoriteAddItemBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private val viewModel: FridgeViewModel by activityViewModels()
    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { handleImagePicked(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FavoriteAddItemBinding.inflate(inflater, container, false)
        setupCategorySpinner()
        setupAddButton()
        setupImagePicker()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupCategorySpinner() {
        val categories = viewModel.categories
        val adapter = CustomArrayAdapter(
            requireContext(), R.layout.simple_spinner_item, categories,
            com.example.fridgeapp.R.font.amaranth
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter
    }

    private fun setupAddButton() {
        binding.addItemButton.setOnClickListener {
            if (validateInput()) {
                addItemToFavorites()
                navigateToExpirationDates()
            }
        }
    }

    private fun setupImagePicker() {
        binding.itemImage.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }
    }

    private fun handleImagePicked(uri: Uri) {
        binding.itemImage.setImageURI(uri)
        requireActivity().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        imageUri = uri
    }

    private fun validateInput(): Boolean {
        val name = binding.productName.text.toString()
        val daysToExpireStr = binding.productDaysToExpire.text.toString()
        val daysToExpire = daysToExpireStr.toIntOrNull()

        return when {
            name.toString().length < 2 -> {
                showToast(getString(com.example.fridgeapp.R.string.please_enter_a_valid_product_name))
                false
            }
            daysToExpire == null || daysToExpire <= 0 -> {
                showToast(getString(com.example.fridgeapp.R.string.please_enter_a_valid_number_of_days_to_expire))
                false
            }
            else -> {
                true
            }
        }
    }

    private fun addItemToFavorites() {
        val name = binding.productName.text.toString()
        val category = binding.productCategory.selectedItem.toString()
        val daysToExpire = binding.productDaysToExpire.text.toString().toInt()
        val photoUrl = imageUri?.toString()

        val foodItem = FoodItem(
            name = name, photoUrl = photoUrl, category = category, daysToExpire = daysToExpire
        )
        viewModel.insertFoodItem(foodItem)
    }

    private fun navigateToExpirationDates() {
        findNavController().navigate(com.example.fridgeapp.R.id.action_addItemToFavoriteFragment_to_defaultExpirationDatesFragment)
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
        val defaultCategory = getString(com.example.fridgeapp.R.string.breads)
        return binding.productName.text.toString().isNotEmpty() ||
                binding.productCategory.selectedItem.toString() != defaultCategory ||
                binding.productDaysToExpire.text.toString().isNotEmpty() ||
                imageUri != null
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
