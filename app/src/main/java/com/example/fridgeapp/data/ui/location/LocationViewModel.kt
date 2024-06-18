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
import com.example.fridgeapp.data.repository.locationImpl.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * LocationViewModel is an AndroidViewModel class responsible for managing and providing location-related
 * data to the UI. It utilizes a repository to fetch the current location, address, and nearest supermarket
 * information. The ViewModel handles location permissions, retrieves the user's location using FusedLocationProviderClient,
 * and updates LiveData objects to reflect changes in the location, address, and loading state. Additionally, it supports
 * coroutine-based asynchronous operations for fetching address and supermarket data, ensuring smooth UI updates.
 */

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LocationRepository = LocationRepository(application)

    val locationLiveData = LocationUpdatesLiveData(application)

    private val _addressData = MutableLiveData<String>()
    val addressData: LiveData<String> = _addressData

    private val _closestSupermarket = MutableLiveData<Supermarket?>()
    val closestSupermarket: LiveData<Supermarket?> = _closestSupermarket

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var _currentLocation: LatLng? = null
    val currentLocation: LatLng?
        get() = _currentLocation

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    fun requestLocation(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val languageCode = Locale.getDefault().language

        _isLoading.value = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    _currentLocation = LatLng(it.latitude, it.longitude)
                    locationLiveData.setLocation(it)
                    fetchAddress(it, languageCode)
                    findClosestSupermarket(_currentLocation!!)
                }
            }
    }

    private fun fetchAddress(location: Location, languageCode: String) {
        viewModelScope.launch {
            val address = repository.fetchAddressFromLocation(location, languageCode)
            _addressData.value = address
            _isLoading.value = false
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
