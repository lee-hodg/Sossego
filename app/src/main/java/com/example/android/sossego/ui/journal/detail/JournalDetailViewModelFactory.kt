package com.example.android.sossego.ui.journal.detail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.database.journal.repository.JournalRepository

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 */
class JournalEntryViewModelFactory(
    private val journalEntryKey: String,
    private val journalRepository: JournalRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalEntryDetailViewModel::class.java)) {
            return JournalEntryDetailViewModel(journalEntryKey, journalRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
