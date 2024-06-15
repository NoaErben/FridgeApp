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
            .setTitle(context.getString(R.string.enter_custom_product_name))
            .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                val customProductName = customProductNameEditText.text.toString()
                if (customProductName.isNotBlank()) {
                    val position = adapter.getPosition(context.getString(R.string.other))
                    if (position != -1) {
                        adapter.remove(context.getString(R.string.other))
                        adapter.insert(customProductName, position)
                        productNameSpinner.setSelection(position)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }


    fun showReplaceDiscardDialog(context: Context, onReplace: () -> Unit, onDiscard: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.item_exists))
            .setMessage(context.getString(R.string.replace_or_discard_message))
            .setPositiveButton(context.getString(R.string.replace)) { dialog, _ ->
                onReplace()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.discard)) { dialog, _ ->
                onDiscard()
                dialog.dismiss()
            }
        builder.create().show()
    }


    fun showInsertNumberDialog(
        context: Context,
        onConfirm: (Int) -> Unit,
        onCancel: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_insert_number, null)
        val numberEditText = dialogView.findViewById<EditText>(R.id.numberEditText)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle(context.getString(R.string.insert_number))
            .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                val numberStr = numberEditText.text.toString()
                if (numberStr.isNotBlank()) {
                    val number = numberStr.toInt()
                    onConfirm(number)
                } else {
                    onCancel()
                }
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                onCancel()
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }


    fun showConfirmLeaveDialog(context: Context, onConfirm: () -> Unit, onCancel: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.confirmation))
            .setMessage(context.getString(R.string.are_you_sure_you_want_to_leave))
            .setPositiveButton(context.getString(R.string.yes)) { _, _ -> onConfirm() }
            .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
                onCancel()
            }
            .create()
            .show()
    }

    fun showResetConfirmationDialog(context: Context, onConfirm: () -> Unit, onCancel: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.reset_to_default_items))
            .setMessage(context.getString(R.string.are_you_sure_you_want_to_reset_to_default_items))
            .setPositiveButton(context.getString(R.string.confirm)) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                onCancel()
                dialog.dismiss()
            }
            .create()
            .show()
    }

}