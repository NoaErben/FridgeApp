package com.example.fridgeapp.data.ui.fridge

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import com.example.fridgeapp.data.ui.utils.Dialogs
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
        setupRecyclerView()
        setupAdapter()
        observeViewModel()
        setupNavigation()
        setupSwipeActions()
        handleBackButtonPress()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapter() {
        fridgeItemAdapter = FridgeItemAdapter(emptyList(), object : FridgeItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                val item = (binding.recyclerView.adapter as FridgeItemAdapter).itemAt(index)
                fbViewModel.setFridgeChosenItem(item)
                findNavController().navigate(R.id.action_fridgeManagerFragment_to_editFridgeItemFragment)
            }

            override fun onItemLongClick(index: Int) {
                showToast(getString(R.string.swipe_to_delete))
            }
        })
        binding.recyclerView.adapter = fridgeItemAdapter
    }

    private fun observeViewModel() {
        fbViewModel.items.observe(viewLifecycleOwner, Observer { items ->
            Log.d("MyTag", "Observed items: ${items.size}")
            val sortedItems = items.sortedBy { it.timeUntilExpiry() }
            fridgeItemAdapter.updateItems(sortedItems)
            if (items.isEmpty()) {
                binding.emptyImageView?.visibility = View.VISIBLE
                binding.emptyTextView?.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyImageView?.visibility = View.GONE
                binding.emptyTextView?.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        })
    }

    private fun setupNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            showPopupMenu(it)
        }

        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_addItemToFridgeFragment)
        }
    }

    private fun setupSwipeActions() {
        ItemTouchHelper(createSwipeCallback()).attachToRecyclerView(binding.recyclerView)
    }

    private fun createSwipeCallback() = object : ItemTouchHelper.Callback() {
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
                            fridgeItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                        }
                    }
                },
                onCancel = {
                    fridgeItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                }
            )
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
//                R.id.create_household -> {
//                    showToast("create household clicked")
//                    true
//                }
//                R.id.join_household -> {
//                    showToast("join household clicked")
//                    true
//                }
                R.id.shopping_list -> {
                    findNavController().navigate(R.id.action_fridgeManagerFragment_to_fridgeShoppingListFragment)
                    showToast("shopping list clicked")
                    true
                }
                R.id.Favorite_items -> {
                    findNavController().navigate(R.id.action_fridgeManagerFragment_to_defaultExpirationDatesFragment)
                    showToast("Favorite items clicked")
                    true
                }
                R.id.My_profile -> {
                    handleProfileClick()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun handleProfileClick() {
        if (fbViewModel.isUserLoggedIn()) {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_myProfileFragment)
            showToast("My profile clicked")
        } else {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_loginFragment)
            showToast("No user logged-in")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun FridgeItem.timeUntilExpiry(): Long {
        val currentTime = System.currentTimeMillis()
        return expiryDate - currentTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleBackButtonPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Dialogs.showConfirmLeaveDialog(requireContext(),
                    onConfirm = { findNavController().popBackStack() },
                    onCancel = { /* Do nothing */ }
                )
            }
        })
    }
}
