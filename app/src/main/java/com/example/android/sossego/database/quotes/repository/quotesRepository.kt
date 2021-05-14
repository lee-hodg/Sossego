package com.example.android.sossego.database.quotes.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.sossego.database.quotes.database.QuoteDatabase
import com.example.android.sossego.database.quotes.database.asDomainModel
import com.example.android.sossego.database.quotes.domain.Quote
import com.example.android.sossego.database.quotes.network.QuotesApi
import com.example.android.sossego.database.quotes.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class QuotesRepository(private val database: QuoteDatabase) {

    /**
     * A list of Quote (domain model).
     */
    val quotes: LiveData<List<Quote>> =
        Transformations.map(database.quoteDatabaseDao.getAll()) {
            it.asDomainModel()
        }

    /**
     * Refresh the quotes stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */
    suspend fun refreshQuotes() {
        withContext(Dispatchers.IO) {
            val latestQuote = database.quoteDatabaseDao.getTodayRecord()
            if(latestQuote === null ) {
                // only get a new quote if we are out of date
                Timber.d("Fetch new quote from API")
                val networkQuote = QuotesApi.retrofitService.getQuoteAsync()
                database.quoteDatabaseDao.insert(networkQuote.asDatabaseModel()[0])
            }
        }
    }
}