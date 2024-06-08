package com.example.fridgeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fridgeapp.data.ui.FridgeItemAdapter
import com.example.fridgeapp.databinding.FridgeFragmentBinding

class FridgeManagerFragment : Fragment() {

    private var _binding: FridgeFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FridgeLiveDataViewModel by activityViewModels()
    private lateinit var fridgeItemAdapter: FridgeItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FridgeFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            // Handle navigation icon press
            showPopupMenu(it)
        }

        // Set up RecyclerView and Adapter
        fridgeItemAdapter = FridgeItemAdapter(emptyList(), object : FridgeItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                // Handle item click
            }

            override fun onItemLongClick(index: Int) {
                // Handle item long click
            }
        })
        binding.recyclerView.apply {
            adapter = fridgeItemAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Observe LiveData from ViewModel
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            fridgeItemAdapter.updateItems(items)
        })

        // Navigation button click listeners
        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_addItemToFridgeFragment)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.create_household -> {
                    Toast.makeText(context, "create household clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.join_household -> {
                    Toast.makeText(context, "join household clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.shopping_list -> {
                findNavController().navigate(R.id.action_fridgeManagerFragment_to_fridgeShoppingListFragment)
                Toast.makeText(context, "shopping list clicked", Toast.LENGTH_SHORT).show()
                true
                }
                R.id.Favorite_items -> {
                    findNavController().navigate(R.id.action_fridgeManagerFragment_to_defaultExpirationDatesFragment)
                    Toast.makeText(context, "Favorite items clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.My_profile -> {
                    findNavController().navigate(R.id.action_fridgeManagerFragment_to_myProfileFragment)
                    Toast.makeText(context, "My profile clicked", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
