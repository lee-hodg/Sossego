package com.example.android.sossego.database.journal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseJournalEntry(
    val userId: String? = null,

    var journalEntryId: String? = null,

    var entryText: String = "",

    val createdDate: Long = System.currentTimeMillis(),
)
