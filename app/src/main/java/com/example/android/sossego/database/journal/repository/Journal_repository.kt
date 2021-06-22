package com.example.android.sossego.database.journal.repository

import com.example.android.sossego.database.AppDatabase
import com.google.firebase.database.ValueEventListener

/**
 *  The repository handling database operations
 */
class JournalRepository(
    private val mAppDatabase: AppDatabase = AppDatabase.getInstance()): IJournalRepository {

    override fun addJournalEntryListValueEventListener(userId: String, valueEventListener: ValueEventListener){
        return mAppDatabase.addJournalEntryListValueEventListener(userId, valueEventListener)
    }

    override fun removeJournalEntryListValueEventListener(userId: String, valueEventListener: ValueEventListener){
        return mAppDatabase.removeJournalEntryListValueEventListener(userId, valueEventListener)
    }

    override fun addJournalEntryDetailValueEventListener(valueEventListener: ValueEventListener,
                                                         journalEntryKey: String){
        return mAppDatabase.addJournalEntryDetailValueEventListener(valueEventListener,
            journalEntryKey)
    }

    override fun createJournalEntry(userId: String): String {
        return mAppDatabase.createJournalEntry(userId)
    }

    override fun removeJournalEntry(journalEntryKey: String){
        mAppDatabase.removeJournalEntry(journalEntryKey)
    }

    override fun updateJournalEntry(journalEntryKey: String, updatedText: String){
        return mAppDatabase.updateJournalEntry(journalEntryKey, updatedText)
    }

    override fun removeJournalEntryDetailValueEventListener(valueEventListener: ValueEventListener,
                                                            journalEntryKey: String){
        return mAppDatabase.removeJournalEntryDetailValueEventListener(valueEventListener,
            journalEntryKey)
    }

        companion object {
        private var instance: JournalRepository? = null

        fun getInstance(): JournalRepository {
            if (instance == null)
                instance = JournalRepository()
            return instance!!
        }
    }
}