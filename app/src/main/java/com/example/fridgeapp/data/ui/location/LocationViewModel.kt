package com.example.fridgeapp.data.ui.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    val locationLiveData = LocationUpdatesLiveData(application)
}
