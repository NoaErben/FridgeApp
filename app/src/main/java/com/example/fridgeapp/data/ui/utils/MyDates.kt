package com.example.fridgeapp.data.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MyDates {

    fun parseDate(dateStr: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun formatLongDateToString(dateInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = Date(dateInMillis)
        return dateFormat.format(date)
    }

}