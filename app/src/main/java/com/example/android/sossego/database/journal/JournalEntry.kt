package com.example.android.sossego.database.journal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "journal_entry_table")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    var journalEntryId: Long = 0L,

    @ColumnInfo(name = "entry_text")
    var entryText: String = "",

    @ColumnInfo(name = "created_date")
    val createdDate: Long = System.currentTimeMillis(),
)
