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
import com.example.fridgeapp.databinding.ViewItemFragmentBinding

class EditFridgeItemFragment : Fragment() {

    private var _binding: ViewItemFragmentBinding? = null
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
        _binding = ViewItemFragmentBinding.inflate(inflater, container, false)

        binding.picButton.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        binding.imageView.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
