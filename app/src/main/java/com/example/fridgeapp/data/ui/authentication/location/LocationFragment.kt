package com.example.fridgeapp.data.ui.authentication.location

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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.LocationBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LocationFragment : Fragment(), OnMapReadyCallback {

    private var binding: LocationBinding by autoCleared()
    private val location: LocationViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
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

        setupLocationObserver()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLocation = LatLng(location.latitude, location.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                            findNearbySupermarkets(currentLocation)
                        }
                    }
            } catch (e: SecurityException) {
                e.printStackTrace()
                // Handle exception
            }
        }
    }

    private fun findNearbySupermarkets(location: LatLng) {
        val placesClient = Places.createClient(requireContext())
        val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.NAME, Place.Field.LAT_LNG))

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
                for (placeLikelihood in response.placeLikelihoods) {
                    val place = placeLikelihood.place
                    if (place.types?.contains(Place.Type.SUPERMARKET) == true) {
                        mMap.addMarker(MarkerOptions().position(place.latLng!!).title(place.name))
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("LocationFragment", "Place not found: ${exception.message}")
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true
                    getDeviceLocation()
                }
            } else {
                // Permission denied, handle accordingly
                // Provide feedback to the user explaining why the permission is necessary
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
