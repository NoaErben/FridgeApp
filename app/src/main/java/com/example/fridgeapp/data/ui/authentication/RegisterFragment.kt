package com.example.fridgeapp.data.ui.authentication


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.User
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.AuthRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterFragment : Fragment() {

    private var _binding: AuthRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var dialog: Dialog
    private val viewModel: FridgeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = AuthRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // Set click listener for login button
        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.length < 2) {
                Toast.makeText(
                    requireContext(), "Please insert a name", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Password needs to be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(
                    requireContext(), "Passwords do not match", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // If passwords are valid, set the text color to green
            val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
            binding.etPassword.setTextColor(greenColor)
            binding.etConfirmPassword.setTextColor(greenColor)

            showProgressBar()
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val user = User(name, email, uid)
                        databaseReference.child(uid).setValue(user).addOnCompleteListener { userTask ->
                            if (userTask.isSuccessful) {
                                hideProgressBar()
                                Toast.makeText(requireContext(), "User registered successfully", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_registerFragment_to_fridgeManagerFragment)
                            } else {
                                hideProgressBar()
                                Toast.makeText(requireContext(), "Failed to register user in database", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    hideProgressBar()
                    Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showProgressBar() {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
