package com.example.android.sossego.database.quotes.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.android.sossego.database.quotes.domain.Quote
import java.util.*

@Entity(tableName = "quote_table")
@TypeConverters(Converter::class)
data class DatabaseQuote(
    @PrimaryKey(autoGenerate = false)
    var quoteId: String,

    val author: String,

    val length: Long,

    val title: String,

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "quote_text")
    val quoteText: String)

fun List<DatabaseQuote>.asDomainModel(): List<Quote> {
    return map {
        Quote(
            author = it.author,
            length = it.length,
            title = it.title,
            quoteText = it.quoteText)
    }
}

fun DatabaseQuote.asDomainModel(): Quote {
    return Quote(author = this.author,
                 length = this.length,
                 title = this.title,
                 quoteText = this.quoteText)
}
