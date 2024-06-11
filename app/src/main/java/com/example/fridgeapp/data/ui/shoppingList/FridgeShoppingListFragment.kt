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
import com.example.fridgeapp.databinding.FridgeShoppingListBinding
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.fridge.FridgeItemAdapter
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.data.ui.viewModels.FbViewModel
import java.text.SimpleDateFormat
import java.util.Locale


class FridgeShoppingListFragment : Fragment() {

    private var _binding: FridgeShoppingListBinding? = null
    private val binding get() = _binding!!

    private val fbViewModel: FbViewModel by activityViewModels()

    private lateinit var cartItemAdapter: CartItemAdapter
    private lateinit var fridgeItemAdapter: FridgeItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FridgeShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up RecyclerView
        val cartRecyclerView = binding.cartRecyclerView
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val expireRecyclerView = binding.expireRecyclerView
        expireRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up Adapter
        fridgeItemAdapter = FridgeItemAdapter(emptyList(), object : FridgeItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
//                val item = (binding.cartRecyclerView.adapter as FridgeItemAdapter).itemAt(index)
//                Toast.makeText(requireActivity(),
//                    item.name, Toast.LENGTH_SHORT).show()
            }

            override fun onItemLongClick(index: Int) {
                Toast.makeText(requireActivity(),
                    "Swipe right to add to cart", Toast.LENGTH_SHORT).show()
            }
        })
        expireRecyclerView.adapter = fridgeItemAdapter

        cartItemAdapter = CartItemAdapter(emptyList(), object : CartItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                val item = (binding.cartRecyclerView.adapter as CartItemAdapter).itemAt(index)
                fbViewModel.setCartChosenItem(item)
                findNavController().navigate(R.id.action_fridgeShoppingListFragment_to_editItemShoppingListFragment)
            }

            override fun onItemLongClick(index: Int) {
                Toast.makeText(requireActivity(),
                    getString(R.string.swipe_to_delete), Toast.LENGTH_SHORT).show()
            }
        })
        cartRecyclerView.adapter = cartItemAdapter

        // Observe LiveData from ViewModel
        fbViewModel.items.observe(viewLifecycleOwner, Observer { items ->
            Log.d("MyTag", "Observed items: ${items.size}")
            // Sort items by daysUntilExpiry
            val sortedItems = items
                .filter { item ->
                    val currentTime = System.currentTimeMillis()
                    val daysUntilExpiry = (item.expiryDate - currentTime) / (1000 * 60 * 60 * 24)
                    daysUntilExpiry <= 2
                }
                .sortedBy { item ->
                    val currentTime = System.currentTimeMillis()
                    item.expiryDate - currentTime
                }
            fridgeItemAdapter.updateItems(sortedItems)
        })

        fbViewModel.cartItems.observe(viewLifecycleOwner, Observer { items ->
            Log.d("MyTag", "Observed items: ${items.size}")
            // Sort items by category
            val sortedItems = items.sortedBy { item ->
                item.category
            }
            cartItemAdapter.updateItems(sortedItems)
        })

        binding.toolbar.setNavigationOnClickListener {
//            showPopupMenu(it)
        }

        // Navigation button click listeners
        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fridgeShoppingListFragment_to_addItemToShoppingList)
        }

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
                val item = (binding.cartRecyclerView.adapter as CartItemAdapter).itemAt(viewHolder.adapterPosition)
                Dialogs.showConfirmDeleteDialog(requireContext(),
                    onConfirm = {
                        fbViewModel.deleteItemFromCartDatabase(item) { result ->
                            result.onSuccess {
                                Toast.makeText(requireContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show()
                            }.onFailure { exception ->
                                Toast.makeText(requireContext(), "Failed to delete item: ${exception.message}", Toast.LENGTH_SHORT).show()
                                (binding.cartRecyclerView.adapter as CartItemAdapter).notifyItemChanged(viewHolder.adapterPosition)
                            }
                        }
                    },
                    onCancel = {
                        (binding.cartRecyclerView.adapter as CartItemAdapter).notifyItemChanged(viewHolder.adapterPosition)
                    }
                )
            }
        }).attachToRecyclerView(binding.cartRecyclerView)

        ItemTouchHelper(object : ItemTouchHelper.Callback() {

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
                        fbViewModel.deleteItemFromFridgeDatabase(item) { result ->
                            result.onSuccess {
                                Toast.makeText(requireContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show()
                            }.onFailure { exception ->
                                Toast.makeText(requireContext(), "Failed to delete item: ${exception.message}", Toast.LENGTH_SHORT).show()
                                (binding.cartRecyclerView.adapter as CartItemAdapter).notifyItemChanged(viewHolder.adapterPosition)
                            }
                        }
                        fbViewModel.saveCartItemToDatabase(
                            item.name!!,
                            quantity,
                            item.category!!,
                            item.amountMeasure!!,
                            System.currentTimeMillis(),
                            item.photoUrl!!,
                            true,
                            item.photoUrl!!.toUri(),
                        ) { result ->
                            result.onSuccess {
                                Toast.makeText(requireContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show()
                            }.onFailure { exception ->
                                Toast.makeText(requireContext(), "Failed to delete item: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onCancel = {
                        fridgeItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                )
            }
        }).attachToRecyclerView(expireRecyclerView)

    }

}
