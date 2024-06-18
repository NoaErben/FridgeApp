package com.example.fridgeapp.data.ui.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.util.Locale

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LocationRepository = LocationRepository(application)

    val locationLiveData = LocationUpdatesLiveData(application)

    private val _addressData = MutableLiveData<String>()
    val addressData: LiveData<String> = _addressData

    private val _closestSupermarket = MutableLiveData<Supermarket?>()
    val closestSupermarket: LiveData<Supermarket?> = _closestSupermarket

    private var _currentLocation: LatLng? = null
    val currentLocation: LatLng?
        get() = _currentLocation

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    fun requestLocation(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle permission request logic here if needed
            return
        }

        val languageCode = Locale.getDefault().language

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    _currentLocation = LatLng(it.latitude, it.longitude)
                    locationLiveData.setLocation(it) // Update the locationLiveData
                    fetchAddress(it, languageCode)
                    findClosestSupermarket(_currentLocation!!)
                }
            }
    }

    private fun fetchAddress(location: Location, languageCode: String) {
        viewModelScope.launch {
            val address = repository.fetchAddressFromLocation(location, languageCode)
            _addressData.value = address
        }
    }

    private fun findClosestSupermarket(location: LatLng) {
        viewModelScope.launch {
            val closestSupermarket = repository.findClosestSupermarket(location)
            _closestSupermarket.value = closestSupermarket
        }
    }

    data class Supermarket(val name: String, val address: String, val location: LatLng)
}
