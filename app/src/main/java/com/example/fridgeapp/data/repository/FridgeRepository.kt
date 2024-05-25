import android.app.Application
import androidx.lifecycle.LiveData
import com.example.fridgeapp.data.local_db.FridgeDao
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

    fun insert(fridgeItem: FridgeItem) {
        fridgeDao?.insert(fridgeItem)
    }

    fun delete(fridgeItem: FridgeItem) {
        fridgeDao?.delete(fridgeItem)
    }

    fun deleteByName(name: String) {
        fridgeDao?.deleteByName(name)
    }

    fun getFridgeItemById(id: Int): FridgeItem? {
        return fridgeDao?.getFridgeItemById(id)
    }

    fun getFridgeItemByName(name: String): FridgeItem? {
        return fridgeDao?.getFridgeItemByName(name)
    }

    fun getFridgeItemsNameList(): List<String>? {
        return fridgeDao?.getFridgeItemsNameList()
    }

    fun update(fridgeItem: FridgeItem) {
        fridgeDao?.update(fridgeItem)
    }

    fun updateName(id: Int, name: String) {
        fridgeDao?.updateName(id, name)
    }

    fun updatePhotoUrl(id: Int, photoUrl: String?) {
        fridgeDao?.updatePhotoUrl(id, photoUrl)
    }

    fun updateBuyingDate(id: Int, buyingDate: Long) {
        fridgeDao?.updateBuyingDate(id, buyingDate)
    }

    fun updateExpiryDate(id: Int, expiryDate: Long) {
        fridgeDao?.updateExpiryDate(id, expiryDate)
    }

    fun updateCountMeasure(id: Int, countMeasure: String) {
        fridgeDao?.updateCountMeasure(id, countMeasure)
    }

    fun updateCategory(id: Int, category: String) {
        fridgeDao?.updateCategory(id, category)
    }

    fun deleteAll() {
        fridgeDao?.deleteAll()
    }

    fun getExpiringFridgeItems(currentTimeMillis: Long): LiveData<List<FridgeItem>>? {
        return fridgeDao?.getExpiringFridgeItems(currentTimeMillis)
    }

}

