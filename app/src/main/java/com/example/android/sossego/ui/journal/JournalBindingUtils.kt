package com.example.android.sossego.ui.journal


import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.sossego.R
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

/**
 * Uses the Glide library to load an image by URL into an [ImageView]
 */
@BindingAdapter("setImageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}