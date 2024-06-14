package com.example.fridgeapp.data.ui.authentication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.fridgeapp.data.ui.LocationUpdatesLiveData

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    val locationLiveData = LocationUpdatesLiveData(application)
}
