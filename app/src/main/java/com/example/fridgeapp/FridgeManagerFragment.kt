package com.example.fridgeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.databinding.FridgeFragmentBinding

class FridgeManagerFragment: Fragment() {

    private var _binding: FridgeFragmentBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FridgeFragmentBinding.inflate(inflater, container, false)
        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_defaultExpirationDatesFragment)
        }

        binding.itemImage.setOnClickListener {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_myProfileFragment)
        }

        return binding.root
    }

}