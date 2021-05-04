package com.example.android.sossego.ui.home.detail

import androidx.lifecycle.*
import com.example.android.sossego.database.GratitudeDatabaseDao
import com.example.android.sossego.database.GratitudeList
import kotlinx.coroutines.launch
import timber.log.Timber

class GratitudeDetailViewModel (
    private val gratitudeListKey: Long = 0L,
    val database: GratitudeDatabaseDao) : ViewModel() {

    val gratitudeList = MediatorLiveData<GratitudeList>()

    // fun getGratitudeList() = gratitudeList

    init {
        Timber.d("GratitudeDetailViewModel fetching gratitudeList by key: $gratitudeListKey")
        gratitudeList.addSource(database.getGratitudeList(gratitudeListKey), gratitudeList::setValue)

        //Timber.d("GratitudeDetailViewModel obtained gratitudeList ${database.get(gratitudeListKey)?.value}")
    }

    /**
     *     Deal with navigation
     */
    /**
     * Variable that tells the fragment whether it should navigate to [GratitudeFragment].
     *
     * This is `private` because we don't want to expose the ability to set [MutableLiveData] to
     * the [Fragment]
     */
    private val _navigateToGratitude = MutableLiveData<Boolean?>()

    /**
     * When true immediately navigate back to the [GratitudeFragment]
     */
    val navigateToGratitudeFragment: LiveData<Boolean?>
        get() = _navigateToGratitude

    /**
     * Call this immediately after navigating to [GratitudeFragment]
     */
    fun doneNavigating() {
        _navigateToGratitude.value = null
    }

}