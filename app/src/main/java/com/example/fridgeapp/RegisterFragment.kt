package com.example.fridgeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.LoginFragmentBinding
import com.example.fridgeapp.databinding.RegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: RegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FridgeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = RegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener for login button
        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val name = binding.etName.text.toString()
            val address = binding.etName.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (name.length < 2) {
                Toast.makeText(
                    requireContext(), "Please insert a name", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Password needs to be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(
                    requireContext(), "Passwords do not match", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // If passwords are valid, set the text color to green
            val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
            binding.etPassword.setTextColor(greenColor)
            binding.etConfirmPassword.setTextColor(greenColor)

            viewModel.signUp(email, password, name, onSuccess = {
                Toast.makeText(requireContext(), "Sign up successful", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerFragment_to_fridgeManagerFragment)
            }, onFailure = { exception ->
                Toast.makeText(
                    requireContext(), "Sign up failed: ${exception.message}", Toast.LENGTH_SHORT
                ).show()
            })
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
