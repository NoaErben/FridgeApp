package com.example.fridgeapp.data.ui.favoritesItems

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.repository.roomImpl.FoodRepositoryRoom
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.databinding.FavoriteExpirationDatesBinding

class FavoriteExpirationFragment : Fragment() {

    private var _binding: FavoriteExpirationDatesBinding? = null
    private val binding get() = _binding!!

    private val favoriteViewModel: FavoriteViewModel by viewModels {
        FavoriteViewModel.FavoriteViewModelFactory(FoodRepositoryRoom(requireActivity().application))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        favoriteViewModel.foodItems?.observe(viewLifecycleOwner, Observer { foodItems ->
            adapter.setItems(foodItems)
        })

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
                val item = (binding.productRecyclerView.adapter as FavoriteItemAdapter).itemAt(viewHolder.adapterPosition)
                Dialogs.showConfirmDeleteDialog(requireContext(),
                    onConfirm = {
                        favoriteViewModel.deleteFoodItem(item)
                    },
                    onCancel = {
                        (binding.productRecyclerView.adapter as FavoriteItemAdapter).notifyItemChanged(viewHolder.adapterPosition)
                    }
                )
            }
        }).attachToRecyclerView(binding.productRecyclerView)

        val toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_defaultExpirationDatesFragment_to_fridgeManagerFragment)
        }

        binding.resetToDefaultBtn.setOnClickListener{
            // Handle reset button click
            Dialogs.showResetConfirmationDialog(requireContext(),
                onConfirm = {
                    // Implement your logic to reset items here
                    Toast.makeText(requireContext(), getString(R.string.resetting_to_default_items), Toast.LENGTH_SHORT).show()

                    // Example logic to reset items
                    favoriteViewModel.resetToDefaultItems()
                },
                onCancel = {
                    // Optional: Handle cancel action if needed
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

