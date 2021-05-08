package com.example.android.sossego.ui.journal.listing

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.database.journal.JournalDatabaseDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 */
class JournalListingViewModelFactory(
    private val dataSource: JournalDatabaseDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalListingViewModel::class.java)) {
            return JournalListingViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
