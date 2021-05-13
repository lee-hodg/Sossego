package com.example.android.sossego.ui.gratitude.listing

import android.app.Application
import androidx.lifecycle.*
import com.example.android.sossego.database.gratitude.GratitudeDatabaseDao
import com.example.android.sossego.database.gratitude.GratitudeList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GratitudeViewModel(
    val database: GratitudeDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    val gratitudeLists = database.getAllGratitudeListsWithElementCount()

    /**
              Navigation from list to detail
     **/
    /**
     * Variable that tells the Fragment to navigate to a specific [GratitudeFragment]
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateToGratitudeListDetail = MutableLiveData<Long?>()
    /**
     * If this is non-null, immediately navigate to [GratitudeFragment] and call [doneNavigating]
     */
    val navigateToGratitudeListDetail
        get() = _navigateToGratitudeListDetail
    /**
     * Call this immediately after navigating to [GratitudeFragment]
     * It will clear the navigation request, so if the user rotates their phone it won't navigate
     * twice.
     */
    fun doneNavigating() {
        _navigateToGratitudeListDetail.value = null
    }

    fun onGratitudeListClicked(gratitudeListId: Long) {
        _navigateToGratitudeListDetail.value = gratitudeListId
    }

    private suspend fun insert(gratitudeList: GratitudeList) {
        withContext(Dispatchers.IO) {
            database.insert(gratitudeList)
        }
    }

    fun addNewGratitudeList(){
        viewModelScope.launch {

            val newGratitudeList = GratitudeList()

            insert(newGratitudeList)

            _navigateToGratitudeListDetail.value  = database.getLatestGratitudeList()?.gratitudeListId
        }
    }

    private suspend fun delete(gratitudeListId: Long) {
        withContext(Dispatchers.IO) {
            database.deleteGratitudeList(gratitudeListId)
        }
    }

    fun deleteGratitudeList(gratitudeListId: Long){
        viewModelScope.launch {

            delete(gratitudeListId)

        }
    }

}