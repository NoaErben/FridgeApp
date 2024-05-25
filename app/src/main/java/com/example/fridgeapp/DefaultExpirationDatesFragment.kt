package com.example.fridgeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fridgeapp.databinding.DefaultExpirationDatesBinding

class DefaultExpirationDatesFragment: Fragment (){

    private  var _binding : DefaultExpirationDatesBinding? = null
    private val binding
 get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DefaultExpirationDatesBinding.inflate(inflater,container,false)
        binding.addProductExpiryBtn.setOnClickListener {

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}