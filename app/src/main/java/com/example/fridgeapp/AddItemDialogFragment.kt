package com.example.fridgeapp

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.fridgeapp.databinding.ActivityMainBinding

class BookTableDialogFragment : DialogFragment() {
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
    }
}
