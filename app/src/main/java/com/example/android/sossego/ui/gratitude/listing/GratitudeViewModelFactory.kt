package com.example.android.sossego.ui.gratitude.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 */
class GratitudeViewModelFactory(
    private val gratitudeRepository: GratitudeRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GratitudeViewModel::class.java)) {
            return GratitudeViewModel(gratitudeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

