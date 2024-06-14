package com.example.fridgeapp.data.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.fridgeapp.R
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.ui.authentication.LocationViewModel
import com.example.fridgeapp.data.ui.favoritesItems.FavoriteViewModel
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var fbViewModel: FbViewModel
    private val mainViewModel: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database
        FridgeDB.getDatabase(this)

        // Initialize ViewModel
//        favoriteViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(FavoriteViewModel::class.java)
        fbViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(FbViewModel::class.java)

        // Set up the navigation controller
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Navigation will be handled in the SplashScreenFragment
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
