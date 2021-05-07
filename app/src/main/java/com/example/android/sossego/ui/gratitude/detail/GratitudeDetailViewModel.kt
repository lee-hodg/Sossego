package com.example.android.sossego.ui.gratitude.detail

import androidx.lifecycle.*
import com.example.android.sossego.database.GratitudeDatabaseDao
import com.example.android.sossego.database.GratitudeItem
import com.example.android.sossego.database.GratitudeList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class GratitudeDetailViewModel (
    private val gratitudeListKey: Long = 0L,
    val database: GratitudeDatabaseDao) : ViewModel() {

    val gratitudeList = MediatorLiveData<GratitudeList>()

    val gratitudeItems: MediatorLiveData<List<GratitudeItem>> = MediatorLiveData<List<GratitudeItem>>()

    // Initial value of EditText for new gratitude items being added
    val newGratitudeItemText = MutableLiveData<String?>()

    // fun getGratitudeList() = gratitudeList

    init {
        Timber.d("GratitudeDetailViewModel fetching gratitudeList by key: $gratitudeListKey")
        gratitudeList.addSource(database.getGratitudeList(gratitudeListKey),
                gratitudeList::setValue)
        gratitudeItems.addSource(database.getAllItemsForGratitudeList(gratitudeListKey),
                gratitudeItems::setValue)

        //Timber.d("GratitudeDetailViewModel obtained gratitudeList ${database.get(gratitudeListKey)?.value}")
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

            newGratitudeItemText.value?.let {
                val newGratitudeItem = GratitudeItem(parentListId = gratitudeListKey,
                        gratitudeText = newGratitudeItemText.value.toString())
                insert(newGratitudeItem)
            }
        }
        // clear
        newGratitudeItemText.value = ""
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


//    fun onNewGratitudeItemDone(gratitudeListId: Long, view: View, actionId: Int, event: KeyEvent?): Boolean {
//
//        val imeAction = when (actionId) {
//            EditorInfo.IME_ACTION_DONE,
//            EditorInfo.IME_ACTION_SEND,
//            EditorInfo.IME_ACTION_GO -> true
//            else -> false
//        }
//
//        val keyDownEvent = event?.keyCode == KeyEvent.KEYCODE_ENTER
//                && event.action == KeyEvent.ACTION_DOWN
//
//        if (imeAction or keyDownEvent){
//            addNewGratitudeItem(view.toString())
//            return true
//        }
//        return false
//    }

}