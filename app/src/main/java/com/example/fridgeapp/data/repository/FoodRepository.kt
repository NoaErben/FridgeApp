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

    fun insert(foodItem: FoodItem) {
        foodDao?.insert(foodItem)
    }

    fun delete(foodItem: FoodItem) {
        foodDao?.delete(foodItem)
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

    fun update(foodItem: FoodItem) {
        foodDao?.update(foodItem)
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

    fun deleteAll() {
        foodDao?.deleteAll()
    }
}
