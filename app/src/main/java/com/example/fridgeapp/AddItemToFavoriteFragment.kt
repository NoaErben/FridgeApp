package com.example.fridgeapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.AddItemFavoriteBinding

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

        val adapter = CustomArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories,
            R.font.amaranth // Custom font resource
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter

        //Defines what happens when the "Add" button is clicked
        binding.addItemButton.setOnClickListener {
            val name = binding.productName.text.toString()
            val daysToExpireStr = binding.productExpiryDate.text.toString()
            val category = binding.productCategory.selectedItem.toString()
            val photoUrl = imageUri?.toString()

            // Check if the name is not empty
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a product name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the days to expire is a valid integer
            val daysToExpire = daysToExpireStr.toIntOrNull()
            if (daysToExpire == null || daysToExpire <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid number of days to expire", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

class CustomArrayAdapter(
    context: Context,
    resource: Int,
    objects: List<String>,
    private val fontResId: Int
) : ArrayAdapter<String>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        applyCustomFont(view)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        applyCustomFont(view)
        return view
    }

    private fun applyCustomFont(view: View) {
        if (view is TextView) {
            val customFont = ResourcesCompat.getFont(context, fontResId)
            view.typeface = customFont
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // Set text size to 20sp
        }
    }
}