package com.example.fridgeapp.data

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.fridgeapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class NearbyStoresFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
//    variable to get location data
    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    to interact with the Places API.
    private lateinit var placesClient: PlacesClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_stores, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
//        Sets up the callback when the map is ready.
        mapView.getMapAsync(this)
//        Initializes the location client.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialize Places API
        Places.initialize(requireContext(), "YOUR_API_KEY")
        placesClient = Places.createClient(requireContext())

        return view
    }

//    Called when the map is ready to be used
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        requestLocationPermission()
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            try {
//                Gets the last known location.
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val currentLatLng = LatLng(it.latitude, it.longitude)
//                            Moves the camera to the current location.
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
//                            Calls the method to show nearby grocery stores.
                            showNearbyGroceryStores(currentLatLng)
                        }
                    }
            } catch (e: SecurityException) {
                e.printStackTrace()
                // Handle the exception appropriately, e.g., show a message to the user
            }
        }
    }

//    Defines a private method to show nearby grocery stores.
    private fun showNearbyGroceryStores(location: LatLng) {
//    Creates a request to find nearby places.
        val request = FindCurrentPlaceRequest.newInstance(
            listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES)
        )

        try {
            placesClient.findCurrentPlace(request).addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    response?.placeLikelihoods?.forEach { placeLikelihood ->
                        val place = placeLikelihood.place
                        //Checks if the place is a grocery store or supermarket.
                        if (place.types?.contains(Place.Type.GROCERY_OR_SUPERMARKET) == true) {
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(place.latLng!!)
                                    .title(place.name)
                            )
                        }
                    }
                }
            })
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Handle the exception appropriately, e.g., show a message to the user
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                // Permission denied, show a message to the user
            }
        }
    }

    // Lifecycle methods for the MapView
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
}
