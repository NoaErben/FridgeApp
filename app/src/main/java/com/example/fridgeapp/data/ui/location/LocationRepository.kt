package com.example.fridgeapp.data.ui.location

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
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

/**
 * LocationRepository is a repository class that provides methods to fetch the user's address from their
 * location and to find the closest supermarket using the Google Places API. It utilizes a Geocoder to
 * convert the location coordinates into a readable address and makes HTTP requests to the Google Places
 * API to search for nearby supermarkets. The repository parses the API response to return the closest
 * supermarket as a Supermarket object.
 */

class LocationRepository(private val context: Context) {

    suspend fun fetchAddressFromLocation(location: Location, languageCode: String): String {
        return withContext(Dispatchers.IO) {
            val locale = if (languageCode == "iw" || languageCode == "he") Locale("iw") else Locale(languageCode)
            val geocoder = Geocoder(context, locale)
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

                // Set the Accept-Language header based on the device's language
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
