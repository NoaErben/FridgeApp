package com.example.fridgeapp.data.ui.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

/**
 * CustomArrayAdapter is a specialized ArrayAdapter for displaying a list of strings with a custom font.
 */

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
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        }
    }
}
