package com.example.fridgeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.AddItemToFridgeBinding

class AddItemToFridgeFragment : Fragment() {

    private var _binding: AddItemToFridgeBinding? = null
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
        _binding = AddItemToFridgeBinding.inflate(inflater, container, false)
        //Defines what happens when the "Add" button is clicked
        binding.addItemButton.setOnClickListener {
            val name = binding.productName.text.toString()
            val count = binding.quantity.text.toString().toIntOrNull() ?: 0
            val countMeasure = binding.amountMeasure.text.toString()
            val buyingDate = binding.buyingDate.text.toString().toLongOrNull() ?: System.currentTimeMillis()
            val expiryDate = binding.productExpiryDate.text.toString().toLongOrNull() ?: System.currentTimeMillis()
            val category = binding.productCategory.selectedItem.toString()
            val photoUrl = imageUri?.toString()

            //create a FridgeItem by the fields
            val fridgeItem = FridgeItem(
                name = name,
                count = count,
                countMeasure = countMeasure,
                photoUrl = photoUrl,
                buyingDate = buyingDate,
                expiryDate = expiryDate,
                category = category
            )

            //add the FridgeItem to viewModel
            //viewModel.insertFridgeItem(fridgeItem)
            //To do navigation-> after click add, add it to the FridgeFragment
//            findNavController().navigate(R.id.action_addItemFragment_to_mainFragment)
        }

        binding.itemImage.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

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
