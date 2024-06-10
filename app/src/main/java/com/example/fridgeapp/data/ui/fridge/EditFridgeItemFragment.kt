package com.example.fridgeapp.data.ui.fridge

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
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.EditItemInFridgeBinding

class EditFridgeItemFragment : Fragment() {

    private var _binding: EditItemInFridgeBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private val viewModel: FridgeViewModel by activityViewModels()


    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                binding.imageView.setImageURI(it)
                requireActivity().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = it
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
//        setupCategorySpinner()
//        setupMeasureSpinner()
//        setupNameSpinner()
//        setupDatePickers()
//        observeChosenFoodItem()
//        setupSaveButton()
//        setupThrowOutButton()
//        setupImagePicker()
//        handleBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
