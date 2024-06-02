package com.example.fridgeapp.data.ui.favoritesItems

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.FavoriteExpirationDatesBinding

class FavoriteExpirationFragment : Fragment() {

    private var _binding: FavoriteExpirationDatesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FridgeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavoriteExpirationDatesBinding.inflate(inflater, container, false)
        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_defaultExpirationDatesFragment_to_addItemToFavoriteFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FavoriteItemAdapter(emptyList(), object : FavoriteItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                val item = (binding.productRecyclerView.adapter as FavoriteItemAdapter).itemAt(index)
                viewModel.setFoodChosenItem(item)
                findNavController().navigate(R.id.action_defaultExpirationDatesFragment_to_editItemFavoriteFragment)
            }

            override fun onItemLongClick(index: Int) {
                Toast.makeText(requireActivity(), "Long click", Toast.LENGTH_SHORT).show()
                // TODO: update
            }
        })

        binding.productRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.productRecyclerView.adapter = adapter

        viewModel.foodItems?.observe(viewLifecycleOwner, Observer { foodItems ->
            adapter.setItems(foodItems)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
