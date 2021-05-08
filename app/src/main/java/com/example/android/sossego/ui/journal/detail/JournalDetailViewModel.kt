package com.example.android.sossego.ui.journal.detail

import android.view.View
import com.example.android.sossego.database.journal.JournalDatabaseDao
import com.example.android.sossego.database.journal.JournalEntry
import androidx.lifecycle.*
import com.example.android.sossego.database.gratitude.GratitudeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class JournalEntryDetailViewModel (
    journalEntryKey: Long = 0L,
    val database: JournalDatabaseDao
) : ViewModel() {

    val journalEntry:  LiveData<JournalEntry?> = database.getJournalEntry(journalEntryKey)

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
            val journalEntryValue = journalEntry.value
            journalEntryValue?.let{database.delete(journalEntryValue)}
        }
    }

    fun deleteJournalEntry() {
        viewModelScope.launch {
            delete()
        }
        // go back to listing
        _navigateToListing.value = true
    }

    private suspend fun update() {
        withContext(Dispatchers.IO) {
            val journalEntryValue = journalEntry.value
            journalEntryValue?.let{database.update(journalEntryValue)}
        }
    }

    fun saveJournalEntry() {
        viewModelScope.launch {
            update()
        }
        // go back to listing
        _navigateToListing.value = true
    }

    fun onFocusChangeListener() = View.OnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            // Do something when edit text lost focus
            // (this includes Enter, clicking away and navigating back)
            val currentText = journalEntry.value?.entryText ?: ""
            if (currentText.isNotBlank()) {
                Timber.d("The text changed to $currentText")
                saveJournalEntry()
            }
        }
    }

}