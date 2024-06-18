package com.example.fridgeapp.data.ui.utils

import android.content.Context
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.FoodItem

/**
 * DeafultFoodList is an object that provides a list of default food items with predefined expiration days and photo URLs.
 */

object DeafultFoodList {

    val defaultExpirationDays = listOf(
        8,   // Bread
        21,  // Butter
        21,  // Eggs
        7,   // Milk
        10,  // Yellow Cheese
        7,   // Yogurt
        7,   // Chicken
        7,   // Ground Beef
        7,   // Carrot
        7,   // Cucumber
        7,   // Lettuce
        7,   // Tomato
        4,   // Salmon
        60,  // Ketchup
        15,   // Mayonnaise
        30   // Mustard
    )

    val defaultPhotoUrls = listOf(
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fbread.jpg?alt=media&token=4e3dac77-cba9-4ce3-bb62-d688e1f17924",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fbutter.jpg?alt=media&token=31f4452c-7b73-4045-b3f3-4307831783c4",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fegg.jpg?alt=media&token=bfc528e0-981d-4c83-9277-56612a4aac4a",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fmilk.jpg?alt=media&token=f80dbc80-881a-4448-a764-ba3773cebf05",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fcheese.jpeg?alt=media&token=38987267-9114-4f8e-8a28-e52d6b430187",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fyogurt.jpg?alt=media&token=4350d6f9-b41b-4622-b403-35dcfa0f5e45",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fchicken_breast.jpg?alt=media&token=312f8292-471f-4aee-9df1-1cc2f436b5e4",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fbeef.jpg?alt=media&token=0e12fcdf-e5ee-4fe8-8f80-327583d82a544",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fcarrot.jpg?alt=media&token=c000e72f-452e-4eaf-a10c-fbec75ba9a58",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fcucumber.jpg?alt=media&token=a2266334-8d6c-4017-b87c-8cdac7d7a9ff",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Flettuce.jpg?alt=media&token=509bbb4b-f4bf-444d-a29e-bb8c8a6bd768",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Ftomato.jpg?alt=media&token=790dbb85-7001-4dc0-905e-7bfd61142ebe",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fsalmon.jpg?alt=media&token=fbf445cb-cc0c-4680-9b07-edf6087c8b54",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fketchup.jpg?alt=media&token=01f3f0a1-06bd-44ec-a910-68ec42f2330a",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fmayonnaise.jpg?alt=media&token=444ee20c-38fc-421e-a930-ca903a9d0355",
        "https://firebasestorage.googleapis.com/v0/b/fridgeapp-ad44a.appspot.com/o/default_images%2Fmustard.jpg?alt=media&token=f3173839-3080-4912-9bf5-2677464d852a"
    )

    fun createDefaultFoodItems(context: Context): List<FoodItem> {
        val defaultFoodNames = context.resources.getStringArray(R.array.default_food_names).toList()
        val defaultFoodCategories = context.resources.getStringArray(R.array.default_food_categories).toList()

        val defaultFoodItems = mutableListOf<FoodItem>()
        for (i in defaultFoodNames.indices) {
            val foodItem = FoodItem(
                name = defaultFoodNames[i],
                category = defaultFoodCategories[i],
                daysToExpire = defaultExpirationDays[i],
                photoUrl = defaultPhotoUrls[i]
            )
            defaultFoodItems.add(foodItem)
        }
        return defaultFoodItems
    }

}