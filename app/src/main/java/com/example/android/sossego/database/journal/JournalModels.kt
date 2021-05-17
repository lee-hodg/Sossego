package com.example.android.sossego.database.journal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseJournalEntry(
    var journalEntryId: String? = null,

    var entryText: String = "",

    val createdDate: Long = System.currentTimeMillis(),
)
