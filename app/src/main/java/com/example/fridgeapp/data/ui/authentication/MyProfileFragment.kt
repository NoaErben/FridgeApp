package com.example.fridgeapp.data.ui.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.AuthMyProfileBinding

/**
 * Fragment for handling user my profile view.
 */
class MyProfileFragment : Fragment() {

    private var binding : AuthMyProfileBinding by autoCleared()

    private val viewModel: AuthenticationViewmodel by viewModels {
        AuthenticationViewmodel.AuthenticationViewmodelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AuthMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        observeCurrentUser()
    }



    private fun setupUi() {
        binding.arrowBack.setOnClickListener {
            findNavController().navigate(R.id.action_myProfileFragment_to_fridgeManagerFragment)
        }

        binding.btnSignOut.setOnClickListener {
            viewModel.signOut()
            findNavController().navigate(R.id.action_myProfileFragment_to_loginFragment)
        }

        binding.btnChangePass.setOnClickListener{
            findNavController().navigate(R.id.action_myProfileFragment_to_setPasswordFragment)
        }
    }

    private fun observeCurrentUser() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvEmail.text = getString(R.string.email_label, viewModel.currUserMail())

                viewModel.getCurrentUserName(
                    onSuccess = { userName ->
                        binding.tvName.text = if (userName != null) getString(R.string.hello_user, userName) else getString(R.string.helloExclamationMark)
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.failed_fetch_user_name),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } else {
                // User is not logged in, show the login screen or update the UI
                Toast.makeText(requireContext(), getString(R.string.please_log_in), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
