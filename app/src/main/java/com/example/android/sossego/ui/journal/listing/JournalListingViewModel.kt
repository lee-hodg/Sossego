package com.example.android.sossego.ui.journal.listing

import androidx.lifecycle.MutableLiveData

import android.app.Application
import androidx.lifecycle.*
import com.example.android.sossego.database.journal.JournalDatabaseDao
import com.example.android.sossego.database.journal.JournalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JournalListingViewModel(
    val database: JournalDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    val journalEntries = database.getAllJournalEntries()

    /**
     * Variable that tells the Fragment to navigate back to the listing
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateListToDetail = MutableLiveData<Long?>()
    val navigateListToDetail
        get() = _navigateListToDetail

    fun doneNavigating() {
        _navigateListToDetail.value = null
    }

    fun onListItemClicked(listItemId: Long) {
        _navigateListToDetail.value = listItemId
    }

    /**
     * Logic to deal with adding a new journal entry
     */
    private suspend fun insert(journalEntry: JournalEntry) {
        withContext(Dispatchers.IO) {
            database.insert(journalEntry)
        }
    }

    fun addNewJournalEntry() {
        viewModelScope.launch {

            val newJournalEntry = JournalEntry()

            insert(newJournalEntry)

            _navigateListToDetail.value = newJournalEntry.journalEntryId
        }
    }

    /**
     * Logic to deal with deleting a journal entry
     */
    private suspend fun delete(journalEntry: JournalEntry) {
        withContext(Dispatchers.IO) {
            database.delete(journalEntry)
        }
    }

    fun deleteJournalEntry(journalEntry: JournalEntry) {
        viewModelScope.launch {
            delete(journalEntry)
        }
    }
}

