package com.example.fridgeapp.data.ui

import android.annotation.SuppressLint
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

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private var _binding: SplashScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load animations
        val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        val ZoomInAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_in_animation)
        val zoomOutAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_out_animation)
        val slideDownAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_down)

        // Apply fade-in animation to the logo
        binding.splashPhone.visibility = View.VISIBLE
        binding.splashText.visibility = View.VISIBLE
        binding.splashText.startAnimation(slideDownAnimation)
        binding.backgroundSplashScreen.visibility = View.VISIBLE
        binding.fridgeHubDescription.visibility = View.GONE

        // Apply fade-out animation to the logo after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashPhone.startAnimation(ZoomInAnimation)
            binding.backgroundSplashScreen.startAnimation(ZoomInAnimation)
        }, 1500)

        // Apply zoom-out animation to the phone and background after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashPhone.startAnimation(zoomOutAnimation)
            binding.backgroundSplashScreen.startAnimation(zoomOutAnimation)
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashPhone.startAnimation(zoomOutAnimation)
            binding.backgroundSplashScreen.startAnimation(zoomOutAnimation)
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.fridgeHubDescription.visibility = View.VISIBLE
            binding.fridgeHubDescription.startAnimation(fadeInAnimation)
        }, 3000)

        // Navigate to the next fragment after the animations are complete
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splashScreen_to_fridgeManagerFragment)
        }, 4000) // Adjust the delay to fit your needs
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


