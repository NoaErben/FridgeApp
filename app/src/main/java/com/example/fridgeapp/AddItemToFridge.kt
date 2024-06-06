package com.example.fridgeapp

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fridgeapp.databinding.ActivityMainBinding
import com.example.fridgeapp.databinding.AddItemToFridgeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddItemToFridge: AppCompatActivity() {

    private lateinit var binding : AddItemToFridgeBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddItemToFridgeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        binding.addItemButton.setOnClickListener{

            val productName = binding.productName.text.toString()
            val quantity  = binding.quantity.text.toString()
            val amountMeasure = binding.amountMeasure.text.toString()
            val buyingDate = binding.buyingDate.text.toString()
            val expiryDate =  binding.productDaysToExpire.text.toString()
//            val productCategory = binding.productCategory.text.toString()


        }

    }
}

