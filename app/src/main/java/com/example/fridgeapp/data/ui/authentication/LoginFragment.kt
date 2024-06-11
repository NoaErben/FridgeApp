package com.example.fridgeapp.data.ui.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.data.ui.viewModels.RoomViewModel
import com.example.fridgeapp.databinding.AuthLoginFragmentBinding

class LoginFragment : Fragment() {

    private var _binding: AuthLoginFragmentBinding? = null
    private val binding get() = _binding!!

    private val fbViewModel: FbViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AuthLoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener for login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailAddress.text.toString()
            val password = binding.etPassword.text.toString()

            fbViewModel.signIn(email, password, onSuccess = {
                // Sign in success, navigate to the next screen or perform any other action
                Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_fridgeManagerFragment)
            }, onFailure = { exception ->
                // If sign in fails, display a message to the user.
                Toast.makeText(requireContext(), "Authentication failed: username or password incorrect", Toast.LENGTH_SHORT).show()
            })
        }

        binding.txtSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.txtForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordInputFragment)
        }

        binding.txtForgotPassword.setOnClickListener {
            val email = binding.etEmailAddress.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sendPasswordResetEmail(email: String) {
        fbViewModel.sendPasswordResetEmail(email, {
            Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
        }, { exception ->
            val errorMessage = exception.message ?: "Error sending password reset email"
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        })
    }
}
