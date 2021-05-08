package com.example.android.sossego.ui.gratitude.detail

import androidx.lifecycle.*
import com.example.android.sossego.database.gratitude.GratitudeDatabaseDao
import com.example.android.sossego.database.gratitude.GratitudeItem
import com.example.android.sossego.database.gratitude.GratitudeList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class GratitudeDetailViewModel (
    private val gratitudeListKey: Long = 0L,
    val database: GratitudeDatabaseDao
) : ViewModel() {

    val gratitudeList:  LiveData<GratitudeList?>

    var gratitudeItems: LiveData<List<GratitudeItem>>

//    var gratitudeItems = MediatorLiveData<List<GratitudeItem>>()
//        set(value) {
//            field = value
//            Timber.d("Setting gratitudeItems value $value")
//        }

    // Initial value of EditText for new gratitude items being added
    val newGratitudeItemText = MutableLiveData<String?>()

    // fun getGratitudeList() = gratitudeList

    init {
        Timber.d("GratitudeDetailViewModel fetching gratitudeList by key: $gratitudeListKey")
//        gratitudeList.addSource(database.getGratitudeList(gratitudeListKey),
//                gratitudeList::setValue)
        gratitudeList = database.getGratitudeList(gratitudeListKey)
//        gratitudeItems.addSource(database.getAllItemsForGratitudeList(gratitudeListKey),
//                gratitudeItems::setValue)
        gratitudeItems = database.getAllItemsForGratitudeList(gratitudeListKey)
        //Timber.d("GratitudeDetailViewModel obtained gratitudeList ${database.get(gratitudeListKey)?.value}")
    }

    /** Should be close the softkeyboard (e.g. after adding new item)?
     *
     */
    private val _hideSoftKeyboard = MutableLiveData<Boolean?>()

    val hideSoftKeyboard: LiveData<Boolean?>
        get() = _hideSoftKeyboard

    fun softKeyboardHidden(){
        _hideSoftKeyboard.value = null
    }

    /**
     *     Deal with navigation
     */
    /**
     * Variable that tells the fragment whether it should navigate to GratitudeFragment.
     *
     * This is `private` because we don't want to expose the ability to set MutableLiveData to
     * the Fragment
     */
    private val _navigateToGratitude = MutableLiveData<Boolean?>()

    /**
     * When true immediately navigate back to the GratitudeFragment
     */
    val navigateToGratitudeFragment: LiveData<Boolean?>
        get() = _navigateToGratitude

    /**
     * Call this immediately after navigating to GratitudeFragment
     */
    fun doneNavigating() {
        _navigateToGratitude.value = null
    }

    private suspend fun deleteItem(gratitudeItemId: Long) {
        withContext(Dispatchers.IO) {
            database.deleteItem(gratitudeItemId)
        }
    }

    fun deleteGratitudeItem(gratitudeItemId: Long) {
        Timber.d("Delete item with id $gratitudeItemId")
        viewModelScope.launch {
                deleteItem(gratitudeItemId)
            }

    }

    private suspend fun insert(gratitudeItem: GratitudeItem) {
        withContext(Dispatchers.IO) {
            database.insertItem(gratitudeItem)
        }
    }

    fun addNewItem(){
        viewModelScope.launch {
            if(!newGratitudeItemText.value.isNullOrEmpty()) {
                val newItemText = newGratitudeItemText.value.toString()
                val newGratitudeItem = GratitudeItem(
                    parentListId = gratitudeListKey,
                    gratitudeText = newItemText)
                insert(newGratitudeItem)
            }
        }

        // hide soft keyboard
        _hideSoftKeyboard.value = true
        // clear
        newGratitudeItemText.value = null
    }

    private suspend fun deleteList() {
        withContext(Dispatchers.IO) {
            database.deleteGratitudeList(gratitudeListKey)
        }
    }

    fun deleteGratitudeList(){
        viewModelScope.launch {
            deleteList()
        }
        // go back to listing
        _navigateToGratitude.value = true
    }

    private suspend fun clearList() {
        withContext(Dispatchers.IO) {
            database.clearGratitudeListItems(gratitudeListKey)
        }
    }

    fun clearGratitudeList(){
        viewModelScope.launch {
            clearList()
        }
    }

    private suspend fun updateItem(gratitudeItem: GratitudeItem) {
        withContext(Dispatchers.IO) {
            database.updateItem(gratitudeItem)
        }
    }

    fun updateGratitudeItem(gratitudeItem: GratitudeItem) {
        Timber.d("Update item with id ${gratitudeItem.gratitudeItemId}")
        viewModelScope.launch {
            updateItem(gratitudeItem)
        }

    }



}