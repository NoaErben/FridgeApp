package com.example.fridgeapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.ui.FridgeViewModel
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

        FirebaseApp.initializeApp(this)

        /*setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            showPopupMenu(it)
        }

         */

        // Initialize the database
        FridgeDB.getDatabase(this)

        // Initialize ViewModel
        fridgeViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(FridgeViewModel::class.java)

        // Show food db context for example
        fridgeViewModel.foodItemsNames?.observe(this, Observer<List<String>> { foodNames ->
            // Update UI with the new food names
            val concatenatedNames = fridgeViewModel.getConcatenatedString()
            // For example, you can update a TextView with the concatenated string
            //binding.textv.setText("From Db: " + concatenatedNames)
        })
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
