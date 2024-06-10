package com.example.fridgeapp.data.ui.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.example.fridgeapp.R

object Dialogs {
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

    fun showCustomProductNameDialog(
        context: Context,
        productNameSpinner: Spinner,
        adapter: ArrayAdapter<String>
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_product_name, null)
        val customProductNameEditText = dialogView.findViewById<EditText>(R.id.customProductNameEditText)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Enter Custom Product Name")
            .setPositiveButton("OK") { dialog, _ ->
                val customProductName = customProductNameEditText.text.toString()
                if (customProductName.isNotBlank()) {
                    val position = adapter.getPosition("Other")
                    if (position != -1) {
                        adapter.remove("Other")
                        adapter.insert(customProductName, position)
                        productNameSpinner.setSelection(position)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }


}