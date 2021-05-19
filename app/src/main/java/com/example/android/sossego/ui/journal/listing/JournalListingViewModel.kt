package com.example.android.sossego.ui.journal.listing

import androidx.lifecycle.MutableLiveData

import android.app.Application
import androidx.lifecycle.*
import com.example.android.sossego.database.journal.FirebaseJournalEntry
import com.example.android.sossego.database.journal.repository.JournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class JournalListingViewModel(
    application: Application,
    private val journalRepository: JournalRepository
) : AndroidViewModel(application) {

    // Monitor if user has authenticated or not
    private val _isUserAuthenticated = MutableLiveData<Boolean>()
    val isUserAuthenticated
        get() = _isUserAuthenticated

    fun userLoggedIn() {
        _isUserAuthenticated.value = true
    }

    fun userLoggedOut() {
        _isUserAuthenticated.value = false
    }


    private val _authenticatedUserId = MutableLiveData<String?>()
    val authenticatedUserId
        get() = _authenticatedUserId

    fun setAuthenticatedUserId(userId: String?){
        _authenticatedUserId.value = userId
    }
    /**
     * Variable that tells the Fragment to navigate back to the listing
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateListToDetail = MutableLiveData<String?>()
    val navigateListToDetail
        get() = _navigateListToDetail

    fun doneNavigating() {
        _navigateListToDetail.value = null
    }

    fun onListItemClicked(listItemId: String) {
        _navigateListToDetail.value = listItemId
    }

    private suspend fun insert(): String {
        val journalEntryKey: String
        withContext(Dispatchers.IO) {
            journalEntryKey = journalRepository.createJournalEntry(_authenticatedUserId.value!!)
        }
        return journalEntryKey
    }

    fun addNewJournalEntry() {
        viewModelScope.launch {
            val journalEntryKey = insert()
            _navigateListToDetail.value = journalEntryKey
        }
    }

    /**
     * Logic to deal with deleting a journal entry
     */
    private suspend fun delete(journalEntryKey: String) {
        withContext(Dispatchers.IO) {
            Timber.d("journalEntryListing removeJournalEntry with key $journalEntryKey")
            journalRepository.removeJournalEntry(journalEntryKey)
        }
    }

    fun deleteJournalEntry(journalEntry: FirebaseJournalEntry) {
        viewModelScope.launch {
            journalEntry.journalEntryId?.let { delete(it) }
        }
    }
}

