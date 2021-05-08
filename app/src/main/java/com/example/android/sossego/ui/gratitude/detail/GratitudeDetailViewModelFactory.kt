package com.example.android.sossego.ui.gratitude.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.database.gratitude.GratitudeDatabaseDao

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the key for the night and the SleepDatabaseDao to the ViewModel.
 */
class GratitudeDetailViewModelFactory(
    private val gratitudeListKey: Long,
    private val dataSource: GratitudeDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GratitudeDetailViewModel::class.java)) {
            return GratitudeDetailViewModel(gratitudeListKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
