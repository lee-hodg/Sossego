package com.example.android.sossego.database.quotes.domain


/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 * database for objects that are mapped to the database
 * network for objects that parse or prepare network calls
 */

/**
 * Quote represent a quote of the day for example.
 */
data class Quote constructor(val quoteText: String,
                             val author: String,
                             val length: Long,
                             val title: String)