package com.example.android.sossego.ui.gratitude.detail

import androidx.lifecycle.*
import com.example.android.sossego.database.gratitude.FirebaseGratitudeItem
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class GratitudeDetailViewModel (
    private val gratitudeListKey: String,
    private val gratitudeRepository: GratitudeRepository
    ) : ViewModel() {

    var gratitudeList: FirebaseGratitudeList? = null


    // Initial value of EditText for new gratitude items being added
    val newGratitudeItemText = MutableLiveData<String?>()

    init {
        Timber.d("GratitudeDetailViewModel fetching gratitudeList by key: $gratitudeListKey")
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

    private suspend fun deleteItem(gratitudeItemId: String) {
        withContext(Dispatchers.IO) {
            Timber.d("Pretend to delete item $gratitudeItemId")
            gratitudeRepository.removeGratitudeItem(gratitudeListKey, gratitudeItemId)
        }
    }

    fun deleteGratitudeItem(gratitudeItemId: String) {
        Timber.d("Delete item with id $gratitudeItemId")
        viewModelScope.launch {
                deleteItem(gratitudeItemId)
            }

    }

    private suspend fun insert(gratitudeItemText: String) {
        withContext(Dispatchers.IO) {
            gratitudeRepository.addGratitudeListItem(gratitudeListKey, gratitudeItemText)
        }
    }

    fun addNewItem(){
        viewModelScope.launch {
            if(!newGratitudeItemText.value.isNullOrEmpty()) {
                val newItemText = newGratitudeItemText.value.toString()
                insert(newItemText)
            }
        }

        // hide soft keyboard
        _hideSoftKeyboard.value = true
        // clear
        newGratitudeItemText.value = null
    }

    private suspend fun deleteList() {
        withContext(Dispatchers.IO) {
            gratitudeRepository.removeGratitudeList(gratitudeListKey)
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
            Timber.d("Clear gratitude items for parent list $gratitudeListKey")
            gratitudeRepository.clearGratitudeItems(gratitudeListKey)
        }
    }

    fun clearGratitudeList(){
        viewModelScope.launch {
            clearList()
        }
    }

    private suspend fun updateItem(gratitudeItem: FirebaseGratitudeItem) {
        withContext(Dispatchers.IO) {
            gratitudeRepository.updateGratitudeItem(gratitudeListKey, gratitudeItem.gratitudeItemId,
                gratitudeItem.gratitudeText)
        }
    }

    fun updateGratitudeItem(gratitudeItem: FirebaseGratitudeItem) {
        Timber.d("Update item with id ${gratitudeItem.gratitudeItemId}")
        viewModelScope.launch {
            updateItem(gratitudeItem)
        }

    }



}