package com.example.fridgeapp.data.ui.authentication.location

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.LocationBinding
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LocationFragment : Fragment() {

    private var binding: LocationBinding by autoCleared()
    private val location: LocationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLocationObserver()
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
                ds.color = ContextCompat.getColor(requireContext(), R.color.black)
                ds.isUnderlineText = true
            }
        }
        spannableString.setSpan(clickableSpan, 0, spannableString.length, 0)
        binding.tvGoogleMapsLink.text = spannableString
        binding.tvGoogleMapsLink.movementMethod = LinkMovementMethod.getInstance()
    }
}
