package com.example.android.sossego.ui.journal


import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.sossego.convertLongToDateString
import com.example.android.sossego.database.journal.FirebaseJournalEntry


private const val maxChar = 18


@BindingAdapter("journalEntrySmartTruncate")
fun TextView.doJournalEntrySmartTruncate(journalEntry: FirebaseJournalEntry?) {
    journalEntry?.let {
        text = if (journalEntry.entryText.length < maxChar) {
            journalEntry.entryText
        } else {
            journalEntry.entryText.substring(0, maxChar) + "..."
        }
    }
}


@BindingAdapter("journalEntryCreatedDate")
fun TextView.setJournalEntryCreatedDate(item: FirebaseJournalEntry?) {
    item?.let {
        text = convertLongToDateString(item.createdDate)
    }
}