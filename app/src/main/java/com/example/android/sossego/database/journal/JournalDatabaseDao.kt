package com.example.android.sossego.database.journal

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.sossego.database.gratitude.GratitudeList

@Dao
interface JournalDatabaseDao {

    @Query("SELECT * from journal_entry_table WHERE journalEntryId = :key")
    fun getJournalEntry(key: Long): LiveData<JournalEntry?>

    @Insert
    suspend fun insert(journalEntry: JournalEntry): Long

    @Update
    suspend fun update(journalEntry: JournalEntry)

    @Delete
    suspend fun delete(journalEntry: JournalEntry)

    @Query("SELECT * FROM journal_entry_table ORDER BY created_date DESC")
    fun getAllJournalEntries(): LiveData<List<JournalEntry>>

}