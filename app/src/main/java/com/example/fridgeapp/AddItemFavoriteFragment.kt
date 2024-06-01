package com.example.fridgeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.AddItemFavoriteBinding
import com.example.fridgeapp.databinding.AddItemToFridgeBinding

class AddItemToFavoriteFragment : Fragment() {

    private var _binding: AddItemFavoriteBinding? = null
    private val binding
        get() = _binding!!

    private var imageUri: Uri? = null

    private val viewModel: FridgeViewModel by activityViewModels()

    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                binding.itemImage.setImageURI(it)
                requireActivity().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                imageUri = it
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddItemFavoriteBinding.inflate(inflater, container, false)

        val categories = viewModel.categories

        // Set the adapter for the productCategory Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter

        //Defines what happens when the "Add" button is clicked
        binding.addItemButton.setOnClickListener {
            val name = binding.productName.text.toString()
            val daysToExpire = binding.productExpiryDate.text.toString().toInt()
            val category = binding.productCategory.selectedItem.toString()
            val photoUrl = imageUri?.toString()


            //create a FridgeItem by the fields
            val foodItem = FoodItem(
                name = name,
                photoUrl = photoUrl,
                category = category,
                daysToExpire = daysToExpire
            )

            //add the FridgeItem to viewModel
            viewModel.insertFoodItem(foodItem)
            //To do navigation-> after click add, add it to the FridgeFragment
            findNavController().navigate(R.id.action_addItemToFavoriteFragment_to_defaultExpirationDatesFragment)
        }

        binding.itemImage.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        binding.productCategory

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
