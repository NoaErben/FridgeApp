package com.example.fridgeapp.data.ui.location

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

/**
 * LocationUpdatesLiveData is a LiveData class that provides continuous location updates to its observers.
 * It uses the FusedLocationProviderClient to request location updates at specified intervals and updates its value
 * with the latest location data. When the LiveData becomes active, it starts requesting location updates, and when it
 * becomes inactive, it stops these updates. Additionally, it provides a method to manually set the location value.
 */

class LocationUpdatesLiveData(application: Application) : LiveData<Location>() {

    private val context = application.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            for (location in locationResult.locations) {
                value = location
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun setLocation(location: Location) {
        value = location
    }
}
