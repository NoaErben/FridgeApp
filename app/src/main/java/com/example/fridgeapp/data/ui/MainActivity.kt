package com.example.fridgeapp.data.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.fridgeapp.R
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.data.ui.viewModels.RoomViewModel
import com.example.fridgeapp.data.ui.MainActivityViewModel
import androidx.core.content.ContextCompat
import com.example.fridgeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var roomViewModel: RoomViewModel
    private lateinit var fbViewModel: FbViewModel
    private val mainViewModel : MainActivityViewModel by viewModels()


    private val locationRequestLauncher : ActivityResultLauncher<String>
            = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        if(it) {
            startLocationService()
        }
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        enableEdgeToEdge()
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        FirebaseApp.initializeApp(this)
//
//        /*setSupportActionBar(binding.toolbar)
//
//        binding.toolbar.setNavigationOnClickListener {
//            showPopupMenu(it)
//        }
//
//         */
//
//        // Initialize the database
//        FridgeDB.getDatabase(this)
//
//        // Initialize ViewModel
//        fridgeViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
//            FridgeViewModel::class.java)
//
//        // Show food db context for example
//        fridgeViewModel.foodItemsNames?.observe(this, Observer<List<String>> { foodNames ->
//            // Update UI with the new food names
//            val concatenatedNames = fridgeViewModel.getConcatenatedString()
//            // For example, you can update a TextView with the concatenated string
//            //binding.textv.setText("From Db: " + concatenatedNames)
//        })

        if(ContextCompat.checkSelfPermission(
                this,Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            startLocationService()
        }
        else {
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }


        // Initialize the database
        FridgeDB.getDatabase(this)

        // Initialize ViewModel
        roomViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            RoomViewModel::class.java)
        fbViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            FbViewModel::class.java)

        // Show food db context for example


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Check if the user is logged in
        if (!fbViewModel.isUserLoggedIn()) {
            // User is logged in, navigate to the FridgeFragment
            navController.navigate(R.id.loginFragment)
        } else {
            // User is not logged in, navigate to the LoginActivity
            navController.navigate(R.id.fridgeManagerFragment)
        }
    }

    private fun startLocationService() {
        Log.d("", "hereeee")
        mainViewModel.address.observe(this) { address ->
            Toast.makeText(this, "Your address is $address", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
