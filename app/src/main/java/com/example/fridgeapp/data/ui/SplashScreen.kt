package com.example.fridgeapp.data.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.fridgeapp.data.ui.authentication.AuthenticationViewmodel
import com.example.fridgeapp.databinding.SplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private var _binding: SplashScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationRequestLauncher: ActivityResultLauncher<String>


    private val viewModel: AuthenticationViewmodel by viewModels {
        AuthenticationViewmodel.AuthenticationViewmodelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        // Register for location permission result
        locationRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startLocationService()
            } else {
                Toast.makeText(requireContext(), requireContext().getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

        // Load animations
        val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val zoomInAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_in_animation)
        val zoomOutAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_out_animation)
        val slideDownAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_down)

        binding.apply {
            // Apply fade-in animation to the logo
            splashPhone.visibility = View.VISIBLE
            splashText.visibility = View.VISIBLE
            splashText.startAnimation(slideDownAnimation)
            backgroundSplashScreen.visibility = View.VISIBLE
            fridgeHubDescription.visibility = View.GONE

            // Apply fade-out animation to the logo after a delay
            Handler(Looper.getMainLooper()).postDelayed({
                splashPhone.startAnimation(zoomInAnimation)
                backgroundSplashScreen.startAnimation(zoomInAnimation)
            }, 1500)

            // Apply zoom-out animation to the phone and background after a delay
            Handler(Looper.getMainLooper()).postDelayed({
                splashPhone.startAnimation(zoomOutAnimation)
                backgroundSplashScreen.startAnimation(zoomOutAnimation)
            }, 2000)

            Handler(Looper.getMainLooper()).postDelayed({
                fridgeHubDescription.visibility = View.VISIBLE
                fridgeHubDescription.startAnimation(fadeInAnimation)
            }, 3000)

            // Navigate to the next fragment after the animations are complete
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    checkLocationPermission()
                }
            }, 4000) // Adjust the delay to fit your needs
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationService()
        } else {
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        navigateNext()
    }

    private fun startLocationService() {
        Log.d("SplashScreenFragment", "Starting location service")
        // Handle location service start logic here
        //todo - ??
    }

    private fun navigateNext() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.splashScreen) {
            if (!viewModel.isUserLoggedIn()) {
                Log.d("SplashScreenFragment", "First time user, navigating to login")
                navController.navigate(R.id.action_splashScreen_to_loginFragment)
                setFirstTimeFlag(false)
            } else {
                Log.d("SplashScreenFragment", "Returning user, navigating to fridge manager")
                navController.navigate(R.id.action_splashScreen_to_fridgeManagerFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setFirstTimeFlag(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean("is_first_time", isFirstTime).apply()
    }
}
