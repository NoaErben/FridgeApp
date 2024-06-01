import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.local_db.FoodDao
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.model.FoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FoodRepository(application: Application) {

    private var foodDao: FoodDao?

    init {
        val db = FridgeDB.getDatabase(application.applicationContext)
        foodDao = db?.foodDao()
    }

    fun getAllFoodItems(): LiveData<List<FoodItem>>? {
        return foodDao?.getAllFoodItems()
    }

    fun insert(foodItem: FoodItem) {
        foodDao?.insert(foodItem)
    }

    suspend fun delete(foodItem: FoodItem) {
        try {
            foodDao?.delete(foodItem)
            Log.d("FoodRepository", "Delete successful")
        } catch (e: Exception) {
            Log.e("FoodRepository", "Delete failed", e)
        }
    }

    fun deleteByName(name: String) {
        foodDao?.deleteByName(name)
    }

    fun getFoodItemById(id: Int): FoodItem? {
        return foodDao?.getFoodItemById(id)
    }

    fun getFoodItemByName(name: String): FoodItem? {
        return foodDao?.getFoodItemByName(name)
    }

    fun getFoodsNameList(): LiveData<List<String>>? {
        return foodDao?.getFoodsNameList()
    }

    suspend fun update(foodItem: FoodItem) {
        try {
            // Log the current values of the food item before update
            Log.d("FoodRepository", "Updating food item: $foodItem")

            // Perform the update operation
            foodDao?.update(foodItem)

            // Log a message indicating that the update was successful
            Log.d("FoodRepository", "Update successful")

            // Optionally, log the updated values of the food item after update
            Log.d("FoodRepository", "Updated food item: $foodItem")
        } catch (e: Exception) {
            // Log an error message if the update operation fails
            Log.e("FoodRepository", "Update failed", e)
        }
    }

    fun updateName(id: Int, name: String) {
        foodDao?.updateName(id, name)
    }

    fun updatePhotoUrl(id: Int, photoUrl: String?) {
        foodDao?.updatePhotoUrl(id, photoUrl)
    }

    fun updateDaysToExpire(id: Int, daysToExpire: Int) {
        foodDao?.updateDaysToExpire(id, daysToExpire)
    }

    fun deleteAllFoodTable() {
        foodDao?.deleteAll()
    }

    fun updateCategory(id: Int, newCategory: String) {
        foodDao?.updateCategory(id, newCategory)
    }
}
