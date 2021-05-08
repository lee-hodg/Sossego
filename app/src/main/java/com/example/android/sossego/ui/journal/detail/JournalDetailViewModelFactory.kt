package com.example.android.sossego.ui.journal.detail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.database.journal.JournalDatabaseDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 */
class JournalEntryViewModelFactory(
    private val journalEntryKey: Long,
    private val dataSource: JournalDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalEntryDetailViewModel::class.java)) {
            return JournalEntryDetailViewModel(journalEntryKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
