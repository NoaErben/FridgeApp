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
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.AuthMyProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyProfileFragment: Fragment() {

    private var _binding: AuthMyProfileBinding? = null
    private val binding
        get() = _binding!!
    private val viewModel: FridgeViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AuthMyProfileBinding.inflate(inflater, container, false)

//        binding.btnSignOut.setOnClickListener{
//            viewModel.signOut()
//            findNavController().navigate(R.id.action_myProfileFragment_to_loginFragment)
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        binding.btnSignOut.setOnClickListener {
            viewModel.signOut()
            findNavController().navigate(R.id.action_myProfileFragment_to_loginFragment)
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Get user name from Firebase and display it
                val uid = user.uid
                databaseReference.child(uid).get().addOnSuccessListener {
                    val userName = it.child("name").value.toString()
                    binding.tvName.text = "Hello, $userName"
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to fetch user name", Toast.LENGTH_SHORT).show()
                }

                // Update UI with user email
                binding.tvEmail.text = "E-mail: ${user.email}"
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