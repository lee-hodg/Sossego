package com.example.android.sossego.database.journal.repository

import com.example.android.sossego.database.BaseRepository
import com.google.firebase.database.ValueEventListener

/**
 *  The repository handling database operations
 */
class JournalRepository private constructor() : BaseRepository() {


    fun addJournalEntryListValueEventListener(userId: String, valueEventListener: ValueEventListener){
        return mAppDatabase.addJournalEntryListValueEventListener(userId, valueEventListener)
    }

    fun addJournalEntryDetailValueEventListener(valueEventListener: ValueEventListener,
                                                journalEntryKey: String){
        return mAppDatabase.addJournalEntryDetailValueEventListener(valueEventListener,
            journalEntryKey)
    }

    fun createJournalEntry(userId: String): String {
        return mAppDatabase.createJournalEntry(userId)
    }

    fun removeJournalEntry(journalEntryKey: String){
        mAppDatabase.removeJournalEntry(journalEntryKey)
    }

    fun updateJournalEntry(journalEntryKey: String, updatedText: String){
        return mAppDatabase.updateJournalEntry(journalEntryKey, updatedText)
    }

    fun removeJournalEntryDetailValueEventListener(valueEventListener: ValueEventListener,
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