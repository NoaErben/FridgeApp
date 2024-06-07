package com.example.fridgeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
