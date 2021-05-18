package com.example.android.sossego.ui.gratitude.listing

import android.app.Application
import androidx.lifecycle.*
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GratitudeViewModel(
    private val gratitudeRepository: GratitudeRepository,
    application: Application
) : AndroidViewModel(application) {


    /**
              Navigation from list to detail
     **/
    /**
     * Variable that tells the Fragment to navigate to a specific [GratitudeFragment]
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateToGratitudeListDetail = MutableLiveData<String?>()
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

    fun onGratitudeListClicked(gratitudeListId: String) {
        _navigateToGratitudeListDetail.value = gratitudeListId
    }

    // Monitor if user has authenticated or not
    private val _isUserAuthenticated = MutableLiveData<Boolean>()

    fun userLoggedIn() {
        _isUserAuthenticated.value = true
    }

    fun userLoggedOut() {
        _isUserAuthenticated.value = false
    }

    val isUserAuthenticated
        get() = _isUserAuthenticated

    init {
        _isUserAuthenticated.value = false
    }

    private suspend fun insert(): String? {
        val gratitudeListId: String?
        withContext(Dispatchers.IO) {
            gratitudeListId = gratitudeRepository.createGratitudeList()
        }
        return gratitudeListId
    }

    fun addNewGratitudeList(){
        viewModelScope.launch {

            val gratitudeListKey = insert()

            _navigateToGratitudeListDetail.value = gratitudeListKey
        }
    }

    private suspend fun delete(gratitudeListId: String) {
        withContext(Dispatchers.IO) {
            gratitudeRepository.removeGratitudeList(gratitudeListId)
        }
    }

    fun deleteGratitudeList(gratitudeListId: String){
        viewModelScope.launch {

            delete(gratitudeListId)

        }
    }

}