package com.example.fridgeapp.data.ui.authentication.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    val locationLiveData = LocationUpdatesLiveData(application)
}
