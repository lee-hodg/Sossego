package com.example.android.sossego.ui.journal.listing

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.database.journal.repository.JournalRepository

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 */
class JournalListingViewModelFactory(
    private val application: Application,
    private val journalRepository: JournalRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalListingViewModel::class.java)) {
            return JournalListingViewModel(
                application,
                journalRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
