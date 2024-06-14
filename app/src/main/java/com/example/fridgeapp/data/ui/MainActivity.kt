package com.example.fridgeapp.data.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.fridgeapp.R
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.ui.authentication.location.Location
import com.example.fridgeapp.data.ui.favoritesItems.FavoriteViewModel
import com.example.fridgeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
