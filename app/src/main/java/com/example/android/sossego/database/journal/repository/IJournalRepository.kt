package com.example.android.sossego.database.journal.repository

import com.google.firebase.database.ValueEventListener

interface IJournalRepository {
    fun addJournalEntryListValueEventListener(
        userId: String,
        valueEventListener: ValueEventListener
    )

    fun removeJournalEntryListValueEventListener(
        userId: String,
        valueEventListener: ValueEventListener
    )

    fun addJournalEntryDetailValueEventListener(
        valueEventListener: ValueEventListener,
        journalEntryKey: String
    )

    fun createJournalEntry(userId: String): String
    fun removeJournalEntry(journalEntryKey: String)
    fun updateJournalEntry(journalEntryKey: String, updatedText: String)
    fun removeJournalEntryDetailValueEventListener(
        valueEventListener: ValueEventListener,
        journalEntryKey: String
    )
}