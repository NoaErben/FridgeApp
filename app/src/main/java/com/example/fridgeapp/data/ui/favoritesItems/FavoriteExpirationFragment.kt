package com.example.fridgeapp.data.ui.favoritesItems

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.FavoriteExpirationDatesBinding

class FavoriteExpirationFragment : Fragment() {

    private var _binding: FavoriteExpirationDatesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FridgeViewModel by activityViewModels()

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
                viewModel.setFoodChosenItem(item)
                findNavController().navigate(R.id.action_defaultExpirationDatesFragment_to_editItemFavoriteFragment)
            }

            override fun onItemLongClick(index: Int) {
                Toast.makeText(requireActivity(),
                    getString(R.string.swipe_to_delete), Toast.LENGTH_SHORT).show()
            }
        })

        binding.productRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.productRecyclerView.adapter = adapter

        viewModel.foodItems?.observe(viewLifecycleOwner, Observer { foodItems ->
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
                DialogUtils.showConfirmDeleteDialog(requireContext(),
                    onConfirm = {
                        viewModel.deleteFoodItem(item)
                    },
                    onCancel = {
                        (binding.productRecyclerView.adapter as FavoriteItemAdapter).notifyItemChanged(viewHolder.adapterPosition)
                    }
                )
            }
        }).attachToRecyclerView(binding.productRecyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

object DialogUtils {
    fun showConfirmDeleteDialog(context: Context, onConfirm: () -> Unit, onCancel: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.are_you_sure_you_want_to_delete_this_item))
            .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                onCancel()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    fun showConfirmDiscardChangesDialog(context: Context, onConfirm: () -> Unit, onCancel: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.are_you_sure_you_want_to_discard_changes))
            .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                onCancel()
                dialog.dismiss()
            }
            .create()
            .show()
    }
}

class CustomArrayAdapter(
    context: Context,
    resource: Int,
    objects: List<String>,
    private val fontResId: Int
) : ArrayAdapter<String>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        applyCustomFont(view)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        applyCustomFont(view)
        return view
    }

    private fun applyCustomFont(view: View) {
        if (view is TextView) {
            val customFont = ResourcesCompat.getFont(context, fontResId)
            view.typeface = customFont
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // Set text size to 20sp
        }
    }
}
