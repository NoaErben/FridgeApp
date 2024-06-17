package com.example.fridgeapp.data.ui.authentication

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.AuthLoginFragmentBinding

/**
 * Fragment for handling user login functionality.
 */
class LoginFragment : Fragment() {

    private var binding : AuthLoginFragmentBinding by autoCleared()


    private lateinit var dialog: Dialog
    private val viewModel: AuthenticationViewmodel by viewModels {
        AuthenticationViewmodel.AuthenticationViewmodelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AuthLoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeForgotPasswordLink()
        handleBackButtonPress()
    }


    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailAddress.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.insert_email_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showProgressBar()
            viewModel.signIn(email, password,
                onSuccess = {
                    hideProgressBar()
                    Toast.makeText(requireContext(), getString(R.string.Sign_in_successful), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_fridgeManagerFragment)
                },
                onFailure = { exception ->
                    hideProgressBar()
                    val errorMessage = getString(R.string.authentication_failed, exception.message)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.txtSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeForgotPasswordLink() {
        binding.txtForgotPassword.setOnClickListener {
            val email = binding.etEmailAddress.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        viewModel.sendPasswordResetEmail(email,
            onSuccess = {
                Toast.makeText(requireContext(), getString(R.string.password_reset_email_sent), Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                val errorMessage = exception.message ?: getString(R.string.error_sending_password_reset_email)
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun handleBackButtonPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Dialogs.showConfirmLeaveDialog(requireContext(),
                        onConfirm = { requireActivity().finish() },
                        onCancel = { /* Do nothing */ }
                    )
                }
            })
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
}
