package com.example.android.sossego.database.quotes.network
import com.example.android.sossego.database.quotes.database.Converter
import com.example.android.sossego.database.quotes.database.DatabaseQuote
import com.example.android.sossego.database.quotes.domain.Quote
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server
 * or formatting objects to send to the server. You should convert these to domain objects before
 * using them.
 */
@JsonClass(generateAdapter = true)
data class NetworkQuoteResponse(val contents: NetworkQuoteContainer)

/**
 * QuoteContainer holds a list of NetworkQuotes.
 *
 * This is to parse first level of our network result which looks like
 *
 * {
 *   "quotes": []
 * }
 */
@JsonClass(generateAdapter = true)
data class NetworkQuoteContainer(val quotes: List<NetworkQuote>)

/**
 * Quotes represent a quote to display on home screen
 */
@JsonClass(generateAdapter = true)
data class NetworkQuote(
    val author: String,
    val quote: String,
    val length: Long,
    val id: String,
    val title: String,
    val date: String,
)

/**
 * Convert Quote results to domain objects
 */
fun NetworkQuoteResponse.asDomainModel(): List<Quote> {
    return contents.quotes.map{
        Quote(quoteText = it.quote,
              author=it.author,
              length=it.length,
              title=it.title,
              )
    }
}

fun NetworkQuoteResponse.asDatabaseModel(): Array<DatabaseQuote> {
    return contents.quotes.map{
        DatabaseQuote(quoteId=it.id,
            quoteText = it.quote,
            author=it.author,
            length=it.length,
            title=it.title,
            date= Converter.toDate(it.date))
    }.toTypedArray()
}


