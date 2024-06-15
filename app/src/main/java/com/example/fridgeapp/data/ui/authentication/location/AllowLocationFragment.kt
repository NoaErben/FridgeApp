package com.example.fridgeapp.data.ui.authentication.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.AllowLocationFragmentBinding

class AllowLocationFragment : Fragment() {

    private var binding: AllowLocationFragmentBinding by autoCleared()

    // Register the permissions callback, which handles the user's response to the system permissions dialog
    private val locationRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            findNavController().navigate(R.id.action_allowLocationFragment_to_locationFragment)
        } else {
            // Do nothing if permission is denied
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AllowLocationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAllowLocation.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            findNavController().navigate(R.id.action_allowLocationFragment_to_locationFragment)
        }
    }
}
