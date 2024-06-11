package com.example.fridgeapp.data.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.databinding.SplashScreenBinding
import android.util.Log

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private var _binding: SplashScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

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
                splashPhone.startAnimation(zoomOutAnimation)
                backgroundSplashScreen.startAnimation(zoomOutAnimation)
            }, 2000)

            Handler(Looper.getMainLooper()).postDelayed({
                fridgeHubDescription.visibility = View.VISIBLE
                fridgeHubDescription.startAnimation(fadeInAnimation)
            }, 3000)

            // Navigate to the next fragment after the animations are complete
            Handler(Looper.getMainLooper()).postDelayed({
                if (findNavController().currentDestination?.id == R.id.splashScreen) {
                    if (isFirstTime()) {
                        Log.d("SplashScreenFragment", "First time user, navigating to login")
                        findNavController().navigate(R.id.action_splashScreen_to_loginFragment)
                        setFirstTimeFlag(false)
                    } else {
                        Log.d("SplashScreenFragment", "Returning user, navigating to fridge manager")
                        findNavController().navigate(R.id.action_splashScreen_to_fridgeManagerFragment)
                    }
                }
            }, 4000) // Adjust the delay to fit your needs
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean("is_first_time", true)
    }

    private fun setFirstTimeFlag(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean("is_first_time", isFirstTime).apply()
    }
}
