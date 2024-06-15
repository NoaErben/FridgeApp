package com.example.fridgeapp.data.ui.authentication.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.Locale


class LocationFragment : Fragment(), OnMapReadyCallback {

    private var binding: LocationBinding by autoCleared()
    private val location: LocationViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var placesClient: PlacesClient
    private lateinit var currentLocation: LatLng
    val currentLocale = Locale.getDefault()
    val languageCode = currentLocale.language


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
                            currentLocation = LatLng(location.latitude, location.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
//                            addMarker(currentLocation, getString(R.string.you_are_here), BitmapDescriptorFactory.HUE_BLUE)
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
        val apiKey = getString(R.string.google_maps_key)
        val locationString = "${location.latitude},${location.longitude}"
        val radius = 5000 // Search radius in meters
        val type = "supermarket"
        val language = Locale.getDefault().language

        val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$locationString" +
                "&radius=$radius" +
                "&type=$type" +
                "&key=$apiKey" +
                "&language=$language"

        NearbySearchTask().execute(urlString)
    }

    private inner class NearbySearchTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg urls: String?): String {
            val urlConnection: HttpURLConnection?
            return try {
                val url = URL(urls[0])
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"

                // Set the Accept-Language header based on the device's language
                urlConnection.setRequestProperty("Accept-Language", Locale.getDefault().language)

                val inputStream = urlConnection.inputStream
                val scanner = Scanner(inputStream).useDelimiter("\\A")
                if (scanner.hasNext()) scanner.next() else ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!result.isNullOrEmpty()) {
                val jsonObject = JSONObject(result)
                val resultsArray = jsonObject.getJSONArray("results")
                if (resultsArray.length() > 0) {
                    val nearestSupermarket = resultsArray.getJSONObject(0)
                    val lat = nearestSupermarket.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                    val lng = nearestSupermarket.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                    val name = nearestSupermarket.getString("name")
                    val address = nearestSupermarket.getString("vicinity")
                    val supermarketLocation = LatLng(lat, lng)

                    addMarker(supermarketLocation, name, BitmapDescriptorFactory.HUE_RED)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(supermarketLocation, 15f))
                    val nearestSupermarketLabel = binding.nearestSupermarketTextView.text.toString()
                    binding.nearestSupermarketTextView.text = "$nearestSupermarketLabel $name , $address"

                } else {
                    Toast.makeText(requireContext(), "No supermarkets found nearby", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Error finding supermarkets", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addMarker(location: LatLng, title: String?, color: Float) {
        val markerOptions = MarkerOptions().position(location).title(title)
            .icon(BitmapDescriptorFactory.defaultMarker(color))
        mMap.addMarker(markerOptions)
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
