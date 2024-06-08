package com.example.fridgeapp.data.ui.shoppingList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fridgeapp.databinding.FavoriteExpirationDatesBinding

class FridgeShoppingListFragment : Fragment() {
    private var _binding: FavoriteExpirationDatesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavoriteExpirationDatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
