package com.example.fridgeapp.data.ui.shoppingList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fridgeapp.databinding.FridgeShoppingListBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ShoppingListViewModel
import com.example.fridgeapp.data.ui.FridgeLiveDataViewModel
import com.example.fridgeapp.data.model.CartItem
import com.google.android.material.appbar.MaterialToolbar


class FridgeShoppingListFragment : Fragment() {

    private lateinit var viewModel: ShoppingListViewModel
    private lateinit var binding: FridgeShoppingListBinding
    private lateinit var adapter: CartItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FridgeShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShoppingListViewModel::class.java)

        adapter = CartItemAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.shoppingList.observe(viewLifecycleOwner, Observer { items ->
            adapter = CartItemAdapter(items)
            binding.recyclerView.adapter = adapter
        })

        binding.addProductExpiryBtn.setOnClickListener {
            // Navigate to AddItemToShoppingListFragment
            findNavController().navigate(R.id.action_fridgeShoppingListFragment_to_addItemToShoppingList)
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_fridgeShoppingListFragment_to_fridgeManagerFragment)
        }
    }
}
