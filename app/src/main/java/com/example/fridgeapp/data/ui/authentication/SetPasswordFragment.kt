package com.example.fridgeapp.data.ui.authentication

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.fridgeapp.databinding.AuthFragmentSetPasswordBinding

class SetPasswordFragment: Fragment() {
    private var _binding: AuthFragmentSetPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: Dialog

    private val viewModel : AuthenticationViewmodel by viewModels {
        AuthenticationViewmodel.AuthenticationViewmodelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = AuthFragmentSetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.setOnClickListener {
            val curPass = binding.etOTP.text.toString().trim()
            val password = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmForgotPassword.text.toString().trim()

            if (validateInputs(curPass, password, confirmPassword)) {
                changePass(curPass, password)
            }
        }
    }

    private fun validateInputs(curPass: String, password: String, confirmPassword: String): Boolean {
        if (curPass.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), getString(R.string.password_length_error), Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    private fun changePass(curPass: String, password: String) {
        showProgressBar()

        viewModel.changePassword(curPass, password,
            onSuccess = {
                hideProgressBar()
                onChangeSuccess()
            },
            onFailure = { exception ->
                hideProgressBar()
                onSignUpFailure(exception)
            }
        )
    }

    private fun onChangeSuccess() {
        Toast.makeText(requireContext(), getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_setPasswordFragment_to_myProfileFragment)
    }


    private fun onSignUpFailure(exception: Exception) {
        val errorMessage = getString(R.string.failed_to_change_password, exception.message)
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
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