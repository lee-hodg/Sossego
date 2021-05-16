package com.example.android.sossego.ui.gratitude.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.database.gratitude.GratitudeDatabaseDao
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the key for the night and the SleepDatabaseDao to the ViewModel.
 */
class GratitudeDetailViewModelFactory(
    private val gratitudeListKey: String,
    private val dataSource: GratitudeDatabaseDao,
    private val gratitudeRepository: GratitudeRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GratitudeDetailViewModel::class.java)) {
            return GratitudeDetailViewModel(gratitudeListKey, dataSource, gratitudeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
