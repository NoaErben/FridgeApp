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
import androidx.core.content.ContextCompat
import com.example.fridgeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel : MainActivityViewModel by viewModels()
    private lateinit var fridgeViewModel: FridgeViewModel

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
    }

    private fun startLocationService() {
        Log.d("", "hereeee")
        viewModel.address.observe(this) { address ->
            Toast.makeText(this, "Your address is $address", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
