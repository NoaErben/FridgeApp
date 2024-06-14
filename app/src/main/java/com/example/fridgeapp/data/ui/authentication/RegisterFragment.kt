package com.example.fridgeapp.data.ui.authentication

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.databinding.AuthRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private var _binding: AuthRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: Dialog

    private val fbViewModel: FbViewModel by activityViewModels()

    private val viewModel : AuthenticationViewmodel by viewModels {
        AuthenticationViewmodel.AuthenticationViewmodelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = AuthRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInputs(name, password, confirmPassword)) {
                signUpUser(email, password, name)
            }
        }
    }

    private fun validateInputs(name: String, password: String, confirmPassword: String): Boolean {
        if (name.length < 2) {
            Toast.makeText(requireContext(), "Please insert a name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), "Password needs to be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun signUpUser(email: String, password: String, name: String) {
        showProgressBar()

        viewModel.signUp(email, password, name,
            onSuccess = {
                hideProgressBar()
                onSignUpSuccess()
            },
            onFailure = { exception ->
                hideProgressBar()
                onSignUpFailure(exception)
            }
        )
    }

    private fun onSignUpSuccess() {
        Toast.makeText(requireContext(), "User registered successfully", Toast.LENGTH_SHORT).show()
        fbViewModel.changeUser()
        // TODO: integrate
        findNavController().navigate(R.id.action_registerFragment_to_fridgeManagerFragment)
    }

    private fun onSignUpFailure(exception: Exception) {
        Toast.makeText(requireContext(), "Failed to save user: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    private fun showProgressBar() {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}