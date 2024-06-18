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
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.SplashScreenBinding

/**
 * SplashScreenFragment is a Fragment class that displays an animated splash screen when the app is launched.
 */

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private var binding : SplashScreenBinding by autoCleared()
    private lateinit var sharedPreferences: SharedPreferences

    private val viewModel: AuthenticationViewmodel by viewModels {
        AuthenticationViewmodel.AuthenticationViewmodelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val zoomInAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_in_animation)
        val zoomOutAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_out_animation)
        val slideDownAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_down)

        binding.apply {
            splashPhone.visibility = View.VISIBLE
            splashText.visibility = View.VISIBLE
            splashText.startAnimation(slideDownAnimation)
            backgroundSplashScreen.visibility = View.VISIBLE
            fridgeHubDescription.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                splashPhone.startAnimation(zoomInAnimation)
                backgroundSplashScreen.startAnimation(zoomInAnimation)
            }, 1500)

            Handler(Looper.getMainLooper()).postDelayed({
                splashPhone.startAnimation(zoomOutAnimation)
                backgroundSplashScreen.startAnimation(zoomOutAnimation)
            }, 2000)

            Handler(Looper.getMainLooper()).postDelayed({
                fridgeHubDescription.visibility = View.VISIBLE
                fridgeHubDescription.startAnimation(fadeInAnimation)
            }, 3000)

            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    navigateNext()
                }
            }, 4000)
        }
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

    private fun setFirstTimeFlag(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean("is_first_time", isFirstTime).apply()
    }
}