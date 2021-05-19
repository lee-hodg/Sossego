package com.example.android.sossego.ui.gratitude

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.sossego.convertLongToDateString
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.example.android.sossego.database.quotes.domain.Quote
import timber.log.Timber


/**
 * Returns a string representing the numeric quality rating.
 */
//fun convertNumericQualityToString(num: Long): String {
//    return num.toString()
//}



@BindingAdapter("gratitudeListIdString")
fun TextView.setGratitudeListIdString(item: FirebaseGratitudeList?) {
    Timber.d("setGratitudeListIdString binding adapter got item ${item?.gratitudeListId}")
    item?.let {
        item.gratitudeListId.also { text = it }
    }
}


@BindingAdapter("gratitudeListCreatedDate")
fun TextView.setGratitudeListCreatedDate(item: FirebaseGratitudeList?) {
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

@BindingAdapter("visibleOrGone")
fun View.visibleOrGone(visible: Boolean) {
    visibility = if(visible) View.VISIBLE else View.GONE
}


@BindingAdapter("visibleOrInvisible")
fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if(visible) View.VISIBLE else View.INVISIBLE
}

fun getEmoji(unicode: Int): String {
    return String(Character.toChars(unicode))
}

@BindingAdapter(value = ["streakText", "userDisplayName"], requireAll = true)
fun TextView.setGreeting(streakCount: Int?, userDisplayName: String?) {
    streakCount?.let {
        text = "Welcome " + userDisplayName + ". Congratulations on your " + streakCount.toString() + " streak! " + getEmoji(0x1F525)
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


