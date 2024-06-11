package com.example.fridgeapp.data.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.fridgeapp.R
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.ui.authentication.LoginFragment
import com.example.fridgeapp.data.ui.fridge.FridgeManagerFragment
import com.example.fridgeapp.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var fridgeViewModel: FridgeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseApp.initializeApp(this)

        /*setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            showPopupMenu(it)
        }

         */

        // Initialize the database
        FridgeDB.getDatabase(this)

        // Initialize ViewModel
        fridgeViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            FridgeViewModel::class.java)

        // Show food db context for example


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Check if the user is logged in
        if (!fridgeViewModel.isUserLoggedIn()) {
            // User is logged in, navigate to the FridgeFragment
            navController.navigate(R.id.loginFragment)
        } else {
            // User is not logged in, navigate to the LoginActivity
            navController.navigate(R.id.fridgeManagerFragment)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.create_household -> {
                // Handle create household action
                true
            }
            R.id.join_household -> {
                // Handle join household action
                true
            }
            R.id.shopping_list -> {
                // Handle shopping list action
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.create_household -> {
                    // Handle create household action
                    true
                }
                R.id.join_household -> {
                    // Handle join household action
                    true
                }
                R.id.shopping_list -> {
                    // Handle shopping list action
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
