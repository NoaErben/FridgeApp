import android.app.Application
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.dao.FridgeDao
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.data.model.FridgeItem

class FridgeRepository(application: Application) {

    private var fridgeDao: FridgeDao?

    init {
        val db = FridgeDB.getDatabase(application.applicationContext)
        fridgeDao = db?.fridgeDao()
    }

    fun getAllFridgeItems(): LiveData<List<FridgeItem>>? {
        return fridgeDao?.getAllFridgeItems()
    }

    suspend fun insert(fridgeItem: FridgeItem) {
        fridgeDao?.insert(fridgeItem)
    }

    suspend fun delete(fridgeItem: FridgeItem) {
        fridgeDao?.delete(fridgeItem)
    }

    suspend fun deleteByName(name: String) {
        fridgeDao?.deleteByName(name)
    }

    suspend fun getFridgeItemById(id: Int): FridgeItem? {
        return fridgeDao?.getFridgeItemById(id)
    }

    suspend fun getFridgeItemByName(name: String): FridgeItem? {
        return fridgeDao?.getFridgeItemByName(name)
    }

    suspend fun getFridgeItemsNameList(): List<String>? {
        return fridgeDao?.getFridgeItemsNameList()
    }

    suspend fun update(fridgeItem: FridgeItem) {
        fridgeDao?.update(fridgeItem)
    }

    suspend fun updateName(id: Int, name: String) {
        fridgeDao?.updateName(id, name)
    }

    suspend fun updatePhotoUrl(id: Int, photoUrl: String?) {
        fridgeDao?.updatePhotoUrl(id, photoUrl)
    }

    suspend fun updateBuyingDate(id: Int, buyingDate: Long) {
        fridgeDao?.updateBuyingDate(id, buyingDate)
    }

    suspend fun updateExpiryDate(id: Int, expiryDate: Long) {
        fridgeDao?.updateExpiryDate(id, expiryDate)
    }

    suspend fun updateCountMeasure(id: Int, countMeasure: String) {
        fridgeDao?.updateCountMeasure(id, countMeasure)
    }

    suspend fun updateCategory(id: Int, category: String) {
        fridgeDao?.updateCategory(id, category)
    }

    suspend fun deleteAll() {
        fridgeDao?.deleteAll()
    }
}
