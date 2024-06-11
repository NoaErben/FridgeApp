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
import com.example.fridgeapp.databinding.AuthMyProfileBinding

class MyProfileFragment: Fragment() {

    private var _binding: AuthMyProfileBinding? = null
    private val binding
        get() = _binding!!
    private val fbViewModel: FbViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AuthMyProfileBinding.inflate(inflater, container, false)

        binding.btnSignOut.setOnClickListener{
            fbViewModel.signOut()
            findNavController().navigate(R.id.action_myProfileFragment_to_loginFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fbViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // User is logged in, navigate to the next screen or update the UI
                binding.tvEmail.text = "E-mail: " + user.email.toString()
//                binding.tvName.text = "Hello, " +
                // Navigate to the next screen
            } else {
                // User is not logged in, show the login screen or update the UI
                Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}