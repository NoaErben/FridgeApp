package com.example.fridgeapp.data.ui.shoppingList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.firebaseImpl.CartRepositoryFirebase
import com.example.fridgeapp.data.ui.fridge.FridgeItemAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.databinding.FridgeShoppingListBinding

class FridgeShoppingListFragment : Fragment() {

    private var _binding: FridgeShoppingListBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartItemAdapter: CartItemAdapter
    private lateinit var fridgeItemAdapter: FridgeItemAdapter

    private val viewModel: ShoppingListViewmodel by activityViewModels {
        ShoppingListViewmodel.ShoppingListViewmodelFactory(CartRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FridgeShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerViews()
        setupAdapters()
        observeViewModel()
        setupNavigation()
        setupSwipeActions()
    }

    private fun setupRecyclerViews() {
        binding.cartRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding.expireRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapters() {
        fridgeItemAdapter = FridgeItemAdapter(emptyList(), object : FridgeItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                // Implement item click action if needed
            }

            override fun onItemLongClick(index: Int) {
                showToast("Swipe right to add to cart")
            }
        })
        binding.expireRecyclerView?.adapter = fridgeItemAdapter

        cartItemAdapter = CartItemAdapter(emptyList(), object : CartItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                val item = (binding.cartRecyclerView?.adapter as CartItemAdapter).itemAt(index)
                viewModel.setCartChosenItem(item)
                findNavController().navigate(R.id.action_fridgeShoppingListFragment_to_editItemShoppingListFragment)
            }

            override fun onItemLongClick(index: Int) {
                showToast(getString(R.string.swipe_to_delete))
            }
        })
        binding.cartRecyclerView?.adapter = cartItemAdapter
    }

    private fun observeViewModel() {
        viewModel.fridgeItems.observe(viewLifecycleOwner, Observer { items ->
            Log.d("MyTag", "Observed items: ${items.size}")
            val sortedItems = items
                .filter { it.isExpiringSoon() }
                .sortedBy { it.timeUntilExpiry() }
            fridgeItemAdapter.updateItems(sortedItems)
        })

        viewModel.cartItems.observe(viewLifecycleOwner, Observer { items ->
            Log.d("Cart-Cart", "Observed items: ${items.size}")
            val sortedItems = items.sortedBy { it.category }
            cartItemAdapter.updateItems(sortedItems)
        })
    }

    private fun setupNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            // Implement navigation if needed
        }
        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fridgeShoppingListFragment_to_addItemToShoppingList)
        }

        val toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_fridgeShoppingListFragment_to_fridgeManagerFragment)
        }
    }

    private fun setupSwipeActions() {
        ItemTouchHelper(createCartSwipeCallback()).attachToRecyclerView(binding.cartRecyclerView)
        ItemTouchHelper(createFridgeSwipeCallback()).attachToRecyclerView(binding.expireRecyclerView)
    }

    private fun createCartSwipeCallback() = object : ItemTouchHelper.Callback() {
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
            val item = (binding.cartRecyclerView?.adapter as CartItemAdapter).itemAt(viewHolder.adapterPosition)
            Dialogs.showConfirmDeleteDialog(requireContext(),
                onConfirm = {
                    viewModel.deleteItemFromCartDatabase(item) { result ->
                        result.onSuccess {
                            showToast(getString(R.string.item_deleted_successfully))
                        }.onFailure { exception ->
                            showToast(getString(R.string.failed_to_delete_item, exception.message))
                            cartItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                        }
                    }
                },
                onCancel = {
                    cartItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                }
            )
        }
    }

    private fun createFridgeSwipeCallback() = object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) = makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.RIGHT)

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val item = fridgeItemAdapter.itemAt(viewHolder.adapterPosition)
            Dialogs.showInsertNumberDialog(requireContext(),
                onConfirm = { quantity ->
                    viewModel.deleteItemFromFridgeDatabase(item) { result ->
                        result.onSuccess {
                            showToast(getString(R.string.item_deleted_successfully))
                        }.onFailure { exception ->
                            showToast(getString(R.string.failed_to_delete_item, exception.message))
                            fridgeItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                        }
                    }
                    viewModel.saveCartItemToDatabase(
                        item.name!!,
                        quantity,
                        item.category!!,
                        item.amountMeasure!!,
                        System.currentTimeMillis(),
                        item.photoUrl!!,
                        true,
                        item.photoUrl!!.toUri(),
                        requireContext(),
                    ) { result ->
                        result.onSuccess {
                            showToast(getString(R.string.item_deleted_successfully))
                        }.onFailure { exception ->
                            showToast(getString(R.string.failed_to_delete_item, exception.message))
                        }
                    }
                },
                onCancel = {
                    fridgeItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                }
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    private fun FridgeItem.timeUntilExpiry(): Long {
        val currentTime = System.currentTimeMillis()
        return expiryDate - currentTime
    }

    private fun FridgeItem.isExpiringSoon(): Boolean {
        val currentTime = System.currentTimeMillis()
        val daysUntilExpiry = (expiryDate - currentTime) / (1000 * 60 * 60 * 24)
        return daysUntilExpiry <= 2
    }
}
