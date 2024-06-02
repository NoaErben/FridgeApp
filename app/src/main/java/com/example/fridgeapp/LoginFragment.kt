package com.example.fridgeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.LoginFragmentBinding

class LoginFragment : Fragment() {

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FridgeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener for login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailAddress.text.toString()
            val password = binding.etPassword.text.toString()

            viewModel.signIn(email, password, onSuccess = {
                // Sign in success, navigate to the next screen or perform any other action
                Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                findNavController().navigate(com.example.fridgeapp.R.id.action_loginFragment_to_fridgeManagerFragment)
            }, onFailure = { exception ->
                // If sign in fails, display a message to the user.
                Toast.makeText(requireContext(), "Authentication failed: username or password incorrect", Toast.LENGTH_SHORT).show()
            })
        }

        binding.txtSignUp.setOnClickListener {
            findNavController().navigate(com.example.fridgeapp.R.id.action_loginFragment_to_registerFragment)
        }

        binding.txtForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordInputFragment)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
