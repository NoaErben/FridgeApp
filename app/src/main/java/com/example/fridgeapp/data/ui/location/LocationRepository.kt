package com.example.fridgeapp.data.ui.location

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.example.fridgeapp.R
import com.example.fridgeapp.data.ui.location.LocationViewModel.Supermarket
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.Scanner

class LocationRepository(private val context: Context) {

    suspend fun fetchAddressFromLocation(location: Location): String {
        return withContext(Dispatchers.IO) {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
        }
    }

    suspend fun findClosestSupermarket(location: LatLng): Supermarket? {
        val apiKey = context.getString(R.string.google_maps_key)
        val locationString = "${location.latitude},${location.longitude}"
        val radius = 5000 // Search radius in meters
        val type = "supermarket"
        val language = Locale.getDefault().language

        val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$locationString" +
                "&radius=$radius" +
                "&type=$type" +
                "&key=$apiKey" +
                "&language=$language"

        val result = fetchNearbySupermarkets(urlString)
        return parseClosestSupermarket(result)
    }

    private suspend fun fetchNearbySupermarkets(urlString: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.setRequestProperty("Accept-Language", Locale.getDefault().language)
                val inputStream = urlConnection.inputStream
                val scanner = Scanner(inputStream).useDelimiter("\\A")
                if (scanner.hasNext()) scanner.next() else ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    private fun parseClosestSupermarket(jsonString: String): Supermarket? {
        val jsonObject = JSONObject(jsonString)
        val resultsArray = jsonObject.getJSONArray("results")
        if (resultsArray.length() > 0) {
            val supermarket = resultsArray.getJSONObject(0)
            val lat = supermarket.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
            val lng = supermarket.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
            val name = supermarket.getString("name")
            val address = supermarket.getString("vicinity")
            return Supermarket(name, address, LatLng(lat, lng))
        }
        return null
    }
}
