package com.example.android.sossego.ui.gratitude.listing

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.database.gratitude.GratitudeDatabaseDao
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 */
class GratitudeViewModelFactory(
    private val dataSource: GratitudeDatabaseDao,
    private val gratitudeRepository: GratitudeRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GratitudeViewModel::class.java)) {
            return GratitudeViewModel(dataSource, gratitudeRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

