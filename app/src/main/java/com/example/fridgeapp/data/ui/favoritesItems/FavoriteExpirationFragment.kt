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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.roomImpl.FoodRepositoryRoom
import com.example.fridgeapp.data.ui.utils.DefaultFoodList
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.FavoriteExpirationDatesBinding

/**
 * FavoriteExpirationFragment is a Fragment class that displays a list of favorite food items along with their expiration dates.
 * It allows users to add new items, edit existing ones, and delete items with swipe gestures. The fragment also includes functionality
 * to reset the list to default items and observes changes in the food items list via a ViewModel.
 */

class FavoriteExpirationFragment : Fragment() {


    private var binding : FavoriteExpirationDatesBinding by autoCleared()

    private val favoriteViewModel: FavoriteViewModel by activityViewModels {
        FavoriteViewModel.FavoriteViewModelFactory(FoodRepositoryRoom(requireActivity().application))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FavoriteExpirationDatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAddProductButton()
        setupRecyclerView()
        setupToolbar()
        setupResetButton()
        observeViewModel()
    }

    private fun setupAddProductButton() {
        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_defaultExpirationDatesFragment_to_addItemToFavoriteFragment)
        }
    }

    private fun setupRecyclerView() {
        val adapter = FavoriteItemAdapter(emptyList(), object : FavoriteItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                val item = (binding.productRecyclerView.adapter as FavoriteItemAdapter).itemAt(index)
                favoriteViewModel.setFoodChosenItem(item)
                findNavController().navigate(R.id.action_defaultExpirationDatesFragment_to_editItemFavoriteFragment)
            }

            override fun onItemLongClick(index: Int) {
                Toast.makeText(requireActivity(),
                    getString(R.string.swipe_to_delete), Toast.LENGTH_SHORT).show()
            }
        })

        binding.productRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.productRecyclerView.adapter = adapter

        ItemTouchHelper(createSwipeCallback(adapter)).attachToRecyclerView(binding.productRecyclerView)
    }

    private fun createSwipeCallback(adapter: FavoriteItemAdapter) = object : ItemTouchHelper.Callback() {
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
            val item = adapter.itemAt(viewHolder.adapterPosition)
            Dialogs.showConfirmDeleteDialog(requireContext(),
                onConfirm = { favoriteViewModel.deleteFoodItem(item) },
                onCancel = { adapter.notifyItemChanged(viewHolder.adapterPosition) }
            )
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_defaultExpirationDatesFragment_to_fridgeManagerFragment)
        }
    }

    private fun setupResetButton() {
        binding.resetToDefaultBtn.setOnClickListener {
            Dialogs.showResetConfirmationDialog(requireContext(),
                onConfirm = {
                    Toast.makeText(requireContext(), getString(R.string.resetting_to_default_items), Toast.LENGTH_SHORT).show()
                    val defaultFoodItems = DefaultFoodList.createDefaultFoodItems(requireContext())
                    favoriteViewModel.resetToDefaultItems(defaultFoodItems)
                },
                onCancel = {}
            )
        }
    }

    private fun observeViewModel() {
        favoriteViewModel.foodItems?.observe(viewLifecycleOwner, Observer { foodItems ->
            (binding.productRecyclerView.adapter as FavoriteItemAdapter).setItems(foodItems)
        })
    }
}
