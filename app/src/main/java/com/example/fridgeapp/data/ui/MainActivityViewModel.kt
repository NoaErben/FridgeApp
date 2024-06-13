package com.example.fridgeapp.data.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val locationLiveData = LocationUpdatesLiveData(application)
}
