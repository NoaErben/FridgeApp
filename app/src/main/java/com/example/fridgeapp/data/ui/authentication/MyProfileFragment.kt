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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.fridgeapp.data.ui.authentication.location.Location
import com.example.fridgeapp.databinding.AuthMyProfileBinding
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MyProfileFragment : Fragment() {

    private var _binding: AuthMyProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthenticationViewmodel by viewModels {
        AuthenticationViewmodel.AuthenticationViewmodelFactory(AuthRepositoryFirebase())
    }
    private val location: Location by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AuthMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLocationObserver()
        setupUi()
        observeCurrentUser()
    }

    private fun setupLocationObserver() {
        location.locationLiveData.observe(viewLifecycleOwner, Observer { address ->
            binding.locationTextView.text = address

            val query = "supermarkets near $address"
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val url = "https://www.google.com/maps/search/?api=1&query=$encodedQuery"

            setupGoogleMapsLink(url)
        })
    }

    private fun setupGoogleMapsLink(url: String) {
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
                binding.tvEmail.text = getString(R.string.email_label, user.email)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
