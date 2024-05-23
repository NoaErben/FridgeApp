import android.app.Application
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.dao.FoodDao
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.model.FoodItem

class FoodRepository(application: Application) {

    private var foodDao: FoodDao?

    init {
        val db = FridgeDB.getDatabase(application.applicationContext)
        foodDao = db?.foodDao()
    }

    fun getAllFoodItems(): LiveData<List<FoodItem>>? {
        return foodDao?.getAllFoodItems()
    }

    suspend fun insert(foodItem: FoodItem) {
        foodDao?.insert(foodItem)
    }

    suspend fun delete(foodItem: FoodItem) {
        foodDao?.delete(foodItem)
    }

    suspend fun deleteByName(name: String) {
        foodDao?.deleteByName(name)
    }

    suspend fun getFoodItemById(id: Int): FoodItem? {
        return foodDao?.getFoodItemById(id)
    }

    suspend fun getFoodItemByName(name: String): FoodItem? {
        return foodDao?.getFoodItemByName(name)
    }

    suspend fun getFoodsNameList(): List<String>? {
        return foodDao?.getFoodsNameList()
    }

    suspend fun update(foodItem: FoodItem) {
        foodDao?.update(foodItem)
    }

    suspend fun updateName(id: Int, name: String) {
        foodDao?.updateName(id, name)
    }

    suspend fun updatePhotoUrl(id: Int, photoUrl: String?) {
        foodDao?.updatePhotoUrl(id, photoUrl)
    }

    suspend fun updateDaysToExpire(id: Int, daysToExpire: Int) {
        foodDao?.updateDaysToExpire(id, daysToExpire)
    }

    suspend fun deleteAll() {
        foodDao?.deleteAll()
    }
}
