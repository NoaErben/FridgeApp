package com.example.fridgeapp.data.ui.authentication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.fridgeapp.data.ui.MainActivityViewModel
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.databinding.AuthMyProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class MyProfileFragment: Fragment() {

    // liraz and noa added here
    private lateinit var tvGoogleMapsLink: TextView
    private lateinit var tvAddress: TextView
    // end

    private var _binding: AuthMyProfileBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val fbViewModel: FbViewModel by activityViewModels()
    private val mainViewModel: MainActivityViewModel by viewModels()

    private val viewModel : AuthenticationViewmodel by viewModels {
        AuthenticationViewmodel.AuthenticationViewmodelFactory(AuthRepositoryFirebase())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // liraz and noa added here
        _binding = AuthMyProfileBinding.inflate(inflater, container, false)

//        binding.btnSignOut.setOnClickListener{
//            viewModel.signOut()
//            findNavController().navigate(R.id.action_myProfileFragment_to_loginFragment)
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // liraz and noa added here
        mainViewModel.locationLiveData.observe(viewLifecycleOwner, Observer { address ->
            binding.locationTextView.text = address

            val query = "supermarkets near $address"
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val url = "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
            binding.tvGoogleMapsLink.text = url

            val message = getString(R.string.find_supermarkets_nearby)
            val spannableString = SpannableString(message)
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(requireContext(), R.color.white)
                    ds.isUnderlineText = true
                }
            }
            spannableString.setSpan(clickableSpan, 0, spannableString.length, 0)
            binding.tvGoogleMapsLink.text = spannableString
            binding.tvGoogleMapsLink.movementMethod = LinkMovementMethod.getInstance()

        })
        //end

        mainViewModel.locationLiveData.observe(viewLifecycleOwner, Observer { address ->
            binding.locationTextView.text = address
        })
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // Handle arrow back button click
        binding.arrowBack.setOnClickListener {
            findNavController().navigate(R.id.action_myProfileFragment_to_fridgeManagerFragment)
        }

        binding.btnSignOut.setOnClickListener {
            viewModel.signOut()
            findNavController().navigate(R.id.action_myProfileFragment_to_loginFragment)
        }

        fbViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            // TODO: change vm, not observing currently
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