package com.example.android.sossego.ui.gratitude.listing

import android.app.Application
import androidx.lifecycle.*
import com.example.android.sossego.database.quotes.database.QuoteDatabase
import com.example.android.sossego.database.quotes.repository.QuotesRepository
import kotlinx.coroutines.launch


class QuotesViewModel(
    val database: QuoteDatabase,
    application: Application
) : AndroidViewModel(application) {

    private val quotesRepository = QuotesRepository(database)

    init {
        viewModelScope.launch {
            quotesRepository.refreshQuotes()
        }
    }

    val quotes = quotesRepository.quotes

    /**
     * Factory for constructing QuotesViewModel with parameters
     */
    class Factory(val database: QuoteDatabase, val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuotesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuotesViewModel(database, app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}