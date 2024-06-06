package com.example.fridgeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.AddItemToFridgeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference

//import org.threeten.bp.LocalDate
//import org.threeten.bp.format.DateTimeFormatter

class AddItemToFridgeFragment : Fragment() {

    private var _binding: AddItemToFridgeBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri : Uri

    private val binding
        get() = _binding!!

    private val viewModel: FridgeViewModel by activityViewModels()

    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                binding.itemImage.setImageURI(it)
                requireActivity().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                imageUri = it
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddItemToFridgeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // Set up the add button click listener
        binding.addItemButton.setOnClickListener {
            val productName = binding.productName.text.toString()
            val quantity = binding.quantity.text.toString().toIntOrNull() ?: 0
            val amountMeasure = binding.amountMeasure.text.toString()
            val buyingDate = binding.buyingDate.text.toString().toLongOrNull() ?: System.currentTimeMillis()
            val expiryDate = binding.productDaysToExpire.text.toString().toLongOrNull() ?: System.currentTimeMillis()
            val productCategory = binding.productCategory.selectedItem.toString()

            // Parse the buying and expiry date strings to LocalDate
//            val buyingDate = buyingDateString.toDate("yyyy-MM-dd") ?: LocalDate.now()
//            val expiryDate = expiryDateString.toDate("yyyy-MM-dd") ?: LocalDate.now()

            // Convert LocalDate to milliseconds (timestamp)
//            val buyingDateMillis = buyingDate.atStartOfDay(org.threeten.bp.ZoneId.systemDefault()).toInstant().toEpochMilli()
//            val expiryDateMillis = expiryDate.atStartOfDay(org.threeten.bp.ZoneId.systemDefault()).toInstant().toEpochMilli()

            val photoUrl = imageUri?.toString()

            // Create a FridgeItem by the fields
            val fridgeItem = FridgeItem(
                name = productName,
                count = quantity,
                countMeasure = amountMeasure,
                photoUrl = photoUrl,
                buyingDate = buyingDate,
                expiryDate = expiryDate,
                category = productCategory
            )
            if (uid != null){
                databaseReference.child(uid).setValue(fridgeItem).addOnCompleteListener{
                    if (it.isSuccessful){

                        uploadItemPic()


                    }
                    else{
                        Toast.makeText(requireContext(), "Failed to add item", Toast.LENGTH_SHORT).show()


                    }
                }
            }

            // Add the FridgeItem to viewModel
            //todo - fix it
//            viewModel.insertFridgeItem(fridgeItem)
            // Navigate to the main fragment (adjust the action ID to your navigation graph)
            findNavController().navigate(R.id.FridgeFragment)
        }

        // Set up the image picker click listener
        binding.itemImage.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        return binding.root
    }

    private fun uploadItemPic() {
       //todo
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
