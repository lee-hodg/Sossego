package com.example.android.sossego.ui.gratitude

import android.provider.Settings.Global.getString
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.sossego.R
import com.example.android.sossego.convertLongToDateString
import com.example.android.sossego.database.gratitude.GratitudeList
import com.example.android.sossego.database.journal.JournalEntry
import com.example.android.sossego.database.quotes.domain.Quote
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

@BindingAdapter("formatQuote")
fun TextView.setQuote(item: Quote?) {
    item?.let {
        text = "\"" + item.quoteText + "\"" + " - " + item.author
    }
}


//@BindingAdapter("gratitudeTextChangedListener")
//fun TextView.setGratitudeTextChangedListener(item: GratitudeItem?) {
//    item?.let {
//        text = item.gratitudeText
//
//    }
//}


//fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
//    this.addTextChangedListener(object : TextWatcher {
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//        }
//
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            Timber.d("The text changed!")
//        }
//
//        override fun afterTextChanged(editable: Editable?) {
//            afterTextChanged.invoke(editable.toString())
//        }
//    })
//}


//@BindingAdapter("onEditorEnterAction")
//fun EditText.onEditorEnterAction(f: Function1<String, Unit>?) {
//
//    if (f == null) setOnEditorActionListener(null)
//    else setOnEditorActionListener { v, actionId, event ->
//
//        val imeAction = when (actionId) {
//            EditorInfo.IME_ACTION_DONE,
//            EditorInfo.IME_ACTION_SEND,
//            EditorInfo.IME_ACTION_GO -> true
//            else -> false
//        }
//
//        val keyDownEvent = event?.keyCode == KeyEvent.KEYCODE_ENTER
//                && event.action == KeyEvent.ACTION_DOWN
//
//        if (imeAction or keyDownEvent)
//            true.also { f(v.editableText.toString()) }
//        else false
//    }
//}


