package com.example.fridgeapp.data.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.LocationBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

class LocationFragment : Fragment(), OnMapReadyCallback {

    private var binding: LocationBinding by autoCleared()
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var placesClient: PlacesClient
    private lateinit var currentLocation: LatLng
    val currentLocale = Locale.getDefault()
    val languageCode = currentLocale.language

    private var isAddressReady = false
    private var isSupermarketReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.root.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Set up back button navigation
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_locationFragment_to_fridgeManagerFragment)
        }

        setupLocationObserver()
        setupLoadingObserver()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            locationViewModel.requestLocation(requireContext())
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setupLocationObserver() {
        locationViewModel.addressData.observe(viewLifecycleOwner, Observer { address ->
            binding.nearestSupermarketTextView.visibility = View.VISIBLE
            binding.tvGoogleMapsLink.visibility = View.VISIBLE

            val locationLabel = getString(R.string.location_label)
            val modifiedAddress = "$locationLabel\n$address\n"
            binding.locationTextView.text = modifiedAddress

            val query = "supermarkets near $address"
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val url = "https://www.google.com/maps/search/?api=1&query=$encodedQuery"

            setupGoogleMapsLink(url)

            isAddressReady = true
            showContentIfReady()
        })

        locationViewModel.closestSupermarket.observe(viewLifecycleOwner, Observer { supermarket ->
            supermarket?.let {
                Log.d("LocationFragment", "Closest supermarket found: ${it.name}, ${it.address}")
                addMarker(it.location, it.name, BitmapDescriptorFactory.HUE_RED)
                // Move camera to the closest supermarket
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it.location, 15f))
                val nearestSupermarketLabel = binding.nearestSupermarketTextView.text.toString()
                binding.nearestSupermarketTextView.text = "$nearestSupermarketLabel\n${it.name},\n${it.address}"

                isSupermarketReady = true
                showContentIfReady()
            }
        })
    }

    private fun setupLoadingObserver() {
        locationViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.cardView.visibility = View.GONE
                binding.map.visibility = View.GONE
            } else {
                showContentIfReady()
            }
        })
    }

    private fun showContentIfReady() {
        if (isAddressReady && isSupermarketReady) {
            binding.progressBar.visibility = View.GONE
            binding.cardView.visibility = View.VISIBLE
            binding.map.visibility = View.VISIBLE
        }
    }

    private fun addMarker(location: LatLng, title: String?, color: Float) {
        val markerOptions = MarkerOptions().position(location).title(title)
            .icon(BitmapDescriptorFactory.defaultMarker(color))
        mMap.addMarker(markerOptions)
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true
                    locationViewModel.requestLocation(requireContext())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
