package com.example.fridgeapp.data.ui.favoritesItems

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.repository.roomImpl.FoodRepositoryRoom
import com.example.fridgeapp.data.ui.favoritesItems.FavoriteViewModel
import com.example.fridgeapp.data.ui.utils.CustomArrayAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.databinding.FavoriteEditItemBinding

class FavoriteEditItemFragment : Fragment() {

    private var _binding: FavoriteEditItemBinding? = null
    private val binding get() = _binding!!
    private val favoriteViewModel: FavoriteViewModel by activityViewModels {
        FavoriteViewModel.FavoriteViewModelFactory(FoodRepositoryRoom(requireActivity().application))
    }
    private var imageUri: Uri? = null

    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { handleImagePicked(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FavoriteEditItemBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        observeChosenFoodItem()
        setupSaveButton()
        setupThrowOutButton()
        handleBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        binding.picButton.setOnClickListener { pickImage() }
        binding.imageView.setOnClickListener { pickImage() }
    }

    private fun pickImage() {
        pickLauncher.launch(arrayOf("image/*"))
    }

    private fun handleImagePicked(uri: Uri) {
        binding.imageView.setImageURI(uri)
        requireActivity().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        imageUri = uri
    }

    private fun setupCategorySpinner() {
        val categories = favoriteViewModel.categories
        val adapter = CustomArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories,
            R.font.amaranth
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter
    }

    private fun observeChosenFoodItem() {
        favoriteViewModel.chosenFoodItem.observe(viewLifecycleOwner) { item ->
            binding.itemName.text = item.name
            binding.nameData.setText(item.name)
            binding.daysToExpireData.setText(item.daysToExpire.toString())
            setCategorySpinnerSelection(item)
            loadImage(item.photoUrl)
        }
    }

    private fun setCategorySpinnerSelection(item: FoodItem) {
        val defaultCategoryIndex = favoriteViewModel.categories.indexOf(item.category)
        binding.productCategory.setSelection(defaultCategoryIndex)
    }

    private fun loadImage(photoUrl: String?) {
        val context = binding.root.context
        Glide.with(context).load(photoUrl?.let { uri ->
            if (uri.contains("drawable")) uri.substringAfter("drawable://").toInt() else uri
        } ?: ContextCompat.getDrawable(context, R.drawable.dish)).circleCrop().into(binding.imageView)
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                updateFoodItem()
                navigateToExpirationDates()
            } else {
                showToast(getString(R.string.please_fill_in_all_fields_correctly))
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.nameData.text.toString()
        val daysToExpire = binding.daysToExpireData.text.toString().toIntOrNull()
        return name.isNotEmpty() && daysToExpire != null
    }

    private fun updateFoodItem() {
        favoriteViewModel.chosenFoodItem.value?.let { item ->
            val newName = binding.nameData.text.toString()
            val newCategory = binding.productCategory.selectedItem.toString()
            val newDaysToExpire = binding.daysToExpireData.text.toString().toInt()

            favoriteViewModel.updateFoodName(item.id, newName)
            favoriteViewModel.updateFoodCategory(item.id, newCategory)
            favoriteViewModel.updateFoodDaysToExpire(item.id, newDaysToExpire)
            imageUri?.let { favoriteViewModel.updateFoodPhotoUrl(item.id, it.toString()) }
        }
    }

    private fun navigateToExpirationDates() {
        findNavController().navigate(R.id.action_editItemFavoriteFragment_to_defaultExpirationDatesFragment)
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
                favoriteViewModel.chosenFoodItem.value?.let { item ->
                    favoriteViewModel.deleteFoodItem(item)
                    navigateToExpirationDates()
                }
            },
            onCancel = { /* Do nothing */ }
        )
    }

    private fun handleBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showDiscardChangesDialog()
                } else {
                    findNavController().popBackStack()
                }
            }
        })
    }

    private fun showDiscardChangesDialog() {
        Dialogs.showConfirmDiscardChangesDialog(
            requireContext(),
            onConfirm = { findNavController().popBackStack() },
            onCancel = { /* Do nothing */ }
        )
    }

    private fun hasUnsavedChanges(): Boolean {
        val currentItem = favoriteViewModel.chosenFoodItem.value

        val unsavedChanges = currentItem?.name != binding.nameData.text.toString() ||
                currentItem?.category != binding.productCategory.selectedItem.toString() ||
                currentItem?.daysToExpire.toString() != binding.daysToExpireData.text.toString() ||
                (currentItem?.photoUrl != imageUri?.toString() && imageUri?.toString() != null)

        if (unsavedChanges){
            Log.d("MyTag", currentItem?.name + " " + binding.nameData.text.toString() + " " +
                    currentItem?.category + " " + binding.productCategory.selectedItem.toString() + " " +
                    currentItem?.daysToExpire.toString() + " " + binding.daysToExpireData.text.toString() + " " +
                    currentItem?.photoUrl + " " + imageUri?.toString() + " " + imageUri?.toString() + " " )
        }

        return unsavedChanges
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
