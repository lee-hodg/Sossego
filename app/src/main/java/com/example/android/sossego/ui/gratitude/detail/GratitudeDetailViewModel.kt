package com.example.android.sossego.ui.gratitude.detail

import androidx.lifecycle.*
import com.example.android.sossego.database.gratitude.FirebaseGratitudeItem
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository
import com.example.android.sossego.wrapEspressoIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class GratitudeDetailViewModel (
    private val gratitudeListKey: String,
    private val gratitudeRepository: GratitudeRepository
    ) : ViewModel(), KoinComponent {

    var gratitudeList: FirebaseGratitudeList? = null

    // Inject this so that in tests it is easy to swap for TestCoroutineDispatcher vs Dispatchers.IO
    // on prod
    private val ioDispatcher: CoroutineDispatcher by inject()


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
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                Timber.d("Pretend to delete item $gratitudeItemId")
                gratitudeRepository.removeGratitudeItem(gratitudeListKey, gratitudeItemId)
            }
        }
    }

    fun deleteGratitudeItem(gratitudeItemId: String) {
        wrapEspressoIdlingResource {
            Timber.d("Delete item with id $gratitudeItemId")
            viewModelScope.launch {
                deleteItem(gratitudeItemId)
            }
        }

    }

    private suspend fun insert(gratitudeItemText: String) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                gratitudeRepository.addGratitudeListItem(gratitudeListKey, gratitudeItemText)
            }
        }
    }

    fun addNewItem(){
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                if (!newGratitudeItemText.value.isNullOrEmpty()) {
                    val newItemText = newGratitudeItemText.value.toString()
                    insert(newItemText)
                }
            }

            // hide soft keyboard
            //_hideSoftKeyboard.value = true
            // clear
            newGratitudeItemText.value = null
        }
    }

    private suspend fun deleteList() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                gratitudeRepository.removeGratitudeList(gratitudeListKey)
            }
        }
    }

    fun deleteGratitudeList(){
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                deleteList()
            }
            // go back to listing
            _navigateToGratitude.value = true
        }
    }

    private suspend fun clearList() {
        wrapEspressoIdlingResource {
            {}
            withContext(ioDispatcher) {
                Timber.d("Clear gratitude items for parent list $gratitudeListKey")
                gratitudeRepository.clearGratitudeItems(gratitudeListKey)
            }
        }
    }

    fun clearGratitudeList(){
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                clearList()
            }
        }
    }

    private suspend fun updateItem(gratitudeItem: FirebaseGratitudeItem) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                gratitudeRepository.updateGratitudeItem(
                    gratitudeListKey, gratitudeItem.gratitudeItemId,
                    gratitudeItem.gratitudeText
                )
            }
        }
    }

    fun updateGratitudeItem(gratitudeItem: FirebaseGratitudeItem) {
        wrapEspressoIdlingResource {
            Timber.d("Update item with id ${gratitudeItem.gratitudeItemId}")
            viewModelScope.launch {
                updateItem(gratitudeItem)
            }
        }

    }



}