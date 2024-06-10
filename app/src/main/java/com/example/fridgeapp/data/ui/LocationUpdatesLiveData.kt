package com.example.fridgeapp.data.ui

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LocationUpdatesLiveData(context: Context) : LiveData<String>() {

    // An instance to access location services.
    private val locationClient: FusedLocationProviderClient
            = LocationServices.getFusedLocationProviderClient(context)

    //Lazily initialized Geocoder to convert coordinates to addresses.
    private val geocoder by lazy {
        Geocoder(context)
    }

    private val job = Job()

    private val scope = CoroutineScope(job + Dispatchers.IO)

    //Location Request
    private val locationRequest =  LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(500)
        .setMaxUpdateDelayMillis(1000)
        .build()


    //Defines how to handle location results this is called when a new location result is available.
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            p0.lastLocation?.let {
//                postValue("${it.latitude}, ${it.longitude}")

                scope.launch {
                    if(Build.VERSION.SDK_INT < 33) {
                        val addresses = geocoder.getFromLocation(
                            it.latitude,
                            it.longitude, 1
                        )
                        postValue(addresses!![0].getAddressLine(0))
                    }
                    else {
                        geocoder.getFromLocation(it.latitude,it.longitude,1) {
                            postValue(it[0].getAddressLine(0))
                        }
                    }
                }

            }
        }
    }

    override fun onActive() {
        super.onActive()
        try {
            locationClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.getMainLooper()
            )
        }catch (e : SecurityException) {
            Log.d("LocationUpdatesLiveData","Missing location permission")
        }

    }

    override fun onInactive() {
        super.onInactive()
        job.cancel()
        locationClient.removeLocationUpdates(locationCallback)
    }
}

