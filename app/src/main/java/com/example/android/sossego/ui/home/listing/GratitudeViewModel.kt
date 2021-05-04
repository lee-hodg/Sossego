package com.example.android.sossego.ui.home.listing

import android.app.Application
import androidx.lifecycle.*
import com.example.android.sossego.database.GratitudeDatabaseDao
import com.example.android.sossego.database.GratitudeList
import kotlinx.coroutines.launch

class GratitudeViewModel(
    val database: GratitudeDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    val gratitudeLists = database.getAllGratitudeLists()


    private var latestGratitudeList = MutableLiveData<GratitudeList?>()


    /**
              Navigation from list to detail
     **/
    /**
     * Variable that tells the Fragment to navigate to a specific [GratitudeListDetailFragment]
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateToGratitudeListDetail = MutableLiveData<Long?>()
    /**
     * If this is non-null, immediately navigate to [GratitudeListDetailFragment] and call [doneNavigating]
     */
    val navigateToGratitudeListDetail
        get() = _navigateToGratitudeListDetail
    /**
     * Call this immediately after navigating to [GratitudeListDetailFragment]
     * It will clear the navigation request, so if the user rotates their phone it won't navigate
     * twice.
     */
    fun doneNavigating() {
        _navigateToGratitudeListDetail.value = null
    }

    fun onGratitudeListClicked(gratitudeListId: Long) {
        _navigateToGratitudeListDetail.value = gratitudeListId
    }

    fun addNewGratitudeList(){
        viewModelScope.launch {

            val newGratitudeList = GratitudeList()

            database.insert(newGratitudeList)

            _navigateToGratitudeListDetail.value  = database.getLatestGratitudeList()?.gratitudeListId
        }
    }

}