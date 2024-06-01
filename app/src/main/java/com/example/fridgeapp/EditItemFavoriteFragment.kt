package com.example.fridgeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.EditItemFavoriteBinding
import com.example.fridgeapp.databinding.EditItemInFridgeBinding

class EditItemFavoriteFragment : Fragment() {

    private val viewModel: FridgeViewModel by activityViewModels()

    private var _binding: EditItemFavoriteBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null

    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                binding.imageView.setImageURI(it)
                requireActivity().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = it
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditItemFavoriteBinding.inflate(inflater, container, false)

        binding.picButton.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        binding.imageView.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = viewModel.categories

        // Set the adapter for the productCategory Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter

        viewModel.chosenFoodItem.observe(viewLifecycleOwner) {
            binding.itemName.text = it.name
            binding.nameData.setText(it.name)
            binding.daysToExpireData.setText(it.daysToExpire.toString())

            val defaultCategoryIndex = categories.indexOf(it.category)

            // Set the default selection for the spinner
            binding.productCategory.setSelection(defaultCategoryIndex)

            if (it.photoUrl != null)
                if (it.photoUrl.contains("drawable"))
                    Glide.with(binding.root).load(it.photoUrl.substringAfter("drawable://").toInt())
                        .circleCrop().into(binding.imageView)
                else
                    Glide.with(binding.root).load(it.photoUrl).circleCrop().error(R.drawable.dish)
                        .into(binding.imageView)
            else
                Glide.with(binding.root)
                    .load(ContextCompat.getDrawable(binding.root.context, R.drawable.dish))
                    .circleCrop().into(binding.imageView)
        }

        binding.saveButton.setOnClickListener {
            // Update the chosenFoodItem with the new values
            viewModel.chosenFoodItem.value?.let { item ->
                val newName = binding.nameData.text.toString()
                val newCategory = binding.productCategory.selectedItem.toString()
                val newDaysToExpire = binding.daysToExpireData.text.toString().toIntOrNull()

                if (newName.isNotEmpty() && newDaysToExpire != null) {
                    // Update individual fields of the food item
                    viewModel.updateFoodName(item.id, newName)
                    viewModel.updateFoodCategory(item.id, newCategory)
                    viewModel.updateFoodDaysToExpire(item.id, newDaysToExpire)
                    // TODO: update URL

                    // Navigate after updating the food item
                    findNavController().navigate(R.id.action_editItemFavoriteFragment_to_defaultExpirationDatesFragment)
                } else {
                    // Handle error: show a message or alert if needed
                    Toast.makeText(context, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.throwOutButton.setOnClickListener {
            viewModel.chosenFoodItem.value?.let { item ->
                viewModel.deleteFoodItem(item)
                findNavController().navigate(R.id.action_editItemFavoriteFragment_to_defaultExpirationDatesFragment)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
