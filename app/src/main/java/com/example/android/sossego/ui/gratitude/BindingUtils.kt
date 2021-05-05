package com.example.android.sossego.ui.gratitude

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.sossego.convertLongToDateString
import com.example.android.sossego.database.GratitudeList
import timber.log.Timber


/**
 * Returns a string representing the numeric quality rating.
 */
fun convertNumericQualityToString(num: Long): String {
    return num.toString()
}



@BindingAdapter("gratitudeListIdString")
fun TextView.setGratitudeListIdString(item: GratitudeList?) {
    Timber.d("setGratitudeListIdString binding adapter got item ${item?.gratitudeListId}")
    item?.let {
        text = convertNumericQualityToString(item.gratitudeListId)
    }
}


@BindingAdapter("gratitudeListCreatedDate")
fun TextView.setGratitudeListCreatedDate(item: GratitudeList?) {
    item?.let {
        text = convertLongToDateString(item.createdDate)
    }
}
