package com.example.fridgeapp.data.ui.fridge

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.viewModels.RoomViewModel
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.databinding.FridgeFragmentBinding

class FridgeManagerFragment : Fragment() {

    private var _binding: FridgeFragmentBinding? = null
    private val binding get() = _binding!!

    private val fbViewModel: FbViewModel by activityViewModels()

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

        // Set up RecyclerView
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up Adapter
        fridgeItemAdapter = FridgeItemAdapter(emptyList(), object : FridgeItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                val item = (binding.recyclerView.adapter as FridgeItemAdapter).itemAt(index)
                fbViewModel.setFridgeChosenItem(item)
                findNavController().navigate(R.id.action_fridgeManagerFragment_to_editFridgeItemFragment)
            }

            override fun onItemLongClick(index: Int) {
                Toast.makeText(requireActivity(),
                    getString(R.string.swipe_to_delete), Toast.LENGTH_SHORT).show()
            }
        })
        recyclerView.adapter = fridgeItemAdapter

        // Observe LiveData from ViewModel
        fbViewModel.items.observe(viewLifecycleOwner, Observer { items ->
            Log.d("MyTag", "Observed items: ${items.size}")
            // Sort items by daysUntilExpiry
            val sortedItems = items.sortedBy { item ->
                val currentTime = System.currentTimeMillis()
                item.expiryDate - currentTime
            }
            fridgeItemAdapter.updateItems(sortedItems)
        })

        binding.toolbar.setNavigationOnClickListener {
            showPopupMenu(it)
        }

        // Navigation button click listeners
        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_addItemToFridgeFragment)
        }

        // Add swipe-to-delete functionality using the provided format
        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) = makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = (binding.recyclerView.adapter as FridgeItemAdapter).itemAt(viewHolder.adapterPosition)
                Dialogs.showConfirmDeleteDialog(requireContext(),
                    onConfirm = {
                        fbViewModel.deleteItemFromFridgeDatabase(item) { result ->
                            result.onSuccess {
                                showToast("Item deleted successfully")
                            }.onFailure { exception ->
                                showToast("Failed to delete item: ${exception.message}")
                                (binding.recyclerView.adapter as FridgeItemAdapter).notifyItemChanged(viewHolder.adapterPosition)
                            }
                        }
                    },
                    onCancel = {
                        (binding.recyclerView.adapter as FridgeItemAdapter).notifyItemChanged(viewHolder.adapterPosition)
                    }
                )
            }
        }).attachToRecyclerView(binding.recyclerView)
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
                    if (fbViewModel.isUserLoggedIn()) {
                        findNavController().navigate(R.id.action_fridgeManagerFragment_to_myProfileFragment)
                        Toast.makeText(context, "My profile clicked", Toast.LENGTH_SHORT).show()
                    } else {
                        findNavController().navigate(R.id.action_fridgeManagerFragment_to_loginFragment)
                        Toast.makeText(context, "No user logged-in", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}