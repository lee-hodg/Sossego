package com.example.android.sossego.ui.journal.detail

import android.view.View
import androidx.lifecycle.*
import com.example.android.sossego.database.journal.FirebaseJournalEntry
import com.example.android.sossego.database.journal.repository.JournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class JournalEntryDetailViewModel (
    private val journalEntryKey: String,
    private val journalRepository: JournalRepository
) : ViewModel() {

    companion object {
        const val TAG = "JournalDetViewModel"
    }

    var journalEntry:  MutableLiveData<FirebaseJournalEntry?> = MutableLiveData()


    /** Should be close the soft keyboard (e.g. after adding new item)?
     *
     */
    private val _hideSoftKeyboard = MutableLiveData<Boolean?>()

    val hideSoftKeyboard: LiveData<Boolean?>
        get() = _hideSoftKeyboard

    fun softKeyboardHidden(){
        _hideSoftKeyboard.value = null
    }

    /**
     * Variable that tells the fragment whether it should navigate to listing.
     *
     * This is `private` because we don't want to expose the ability to set MutableLiveData to
     * the Fragment
     */
    private val _navigateToListing = MutableLiveData<Boolean?>()
    val navigateToListing: LiveData<Boolean?>
        get() = _navigateToListing
    fun doneNavigating() {
        _navigateToListing.value = null
    }

    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            Timber.tag(TAG).d("delete with key $journalEntryKey")
            journalRepository.removeJournalEntry(journalEntryKey)
        }
    }

    fun deleteJournalEntry() {
        viewModelScope.launch {
            delete()
        }

        // go back to listing
        _hideSoftKeyboard.value = true
        _navigateToListing.value = true
    }

    private suspend fun update() {
        withContext(Dispatchers.IO) {
            val journalEntryValue = journalEntry.value?.entryText
            journalEntryValue.let{
                if (journalEntryValue != null) {
                    journalRepository.updateJournalEntry(journalEntryKey,
                        journalEntryValue)
                }
            }
        }
    }

    fun saveJournalEntry() {
        viewModelScope.launch {
            update()
        }
        // go back to listing
        _navigateToListing.value = true
        _hideSoftKeyboard.value = true

    }

    fun onFocusChangeListener() = View.OnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            // Do something when edit text lost focus
            // (this includes Enter, clicking away and navigating back)
            val currentText = journalEntry.value?.entryText
            if (currentText != null) {
                if (currentText.isNotBlank()) {
                    Timber.tag(TAG).d("The text changed to $currentText")
                    saveJournalEntry()
                }
            }
        }
    }

}