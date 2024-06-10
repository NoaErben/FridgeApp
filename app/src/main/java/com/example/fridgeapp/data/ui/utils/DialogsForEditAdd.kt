package com.example.fridgeapp.data.ui.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.fridgeapp.R

object DialogsForEditAdd {
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