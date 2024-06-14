package com.example.fridgeapp.data.ui.authentication.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class Location(application: Application) : AndroidViewModel(application) {
    val locationLiveData = LocationUpdatesLiveData(application)
}
