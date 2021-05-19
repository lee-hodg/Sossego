package com.example.android.sossego.database.gratitude.repository

import com.example.android.sossego.database.BaseRepository
import com.google.firebase.database.ValueEventListener

/**
 *  The repository handling database operations
 */
class GratitudeRepository private constructor() : BaseRepository() {

    fun addGratitudeListValueEventListener(userId: String, valueEventListener: ValueEventListener){
        return mAppDatabase.addGratitudeListValueEventListener(userId, valueEventListener)
    }

    fun removeGratitudeListValueEventListener(userId: String, valueEventListener: ValueEventListener){
        return mAppDatabase.removeGratitudeListValueEventListener(userId, valueEventListener)
    }

    fun addGratitudeListDetailValueEventListener(valueEventListener: ValueEventListener,
                                                 childKey: String){
        return mAppDatabase.addGratitudeListDetailValueEventListener(valueEventListener, childKey)
    }

    fun createGratitudeList(userId: String): String {
        return mAppDatabase.createGratitudeList(userId=userId)
    }

    fun removeGratitudeList(parentListKey: String) {
        mAppDatabase.removeGratitudeList(parentListKey)
    }

    fun addGratitudeListItem(parentListId: String, gratitudeItemText: String) {
        return mAppDatabase.addGratitudeItem(parentListId, gratitudeItemText)
    }

    fun updateGratitudeItem(parentListKey: String, childItemKey: String, updatedText: String){
        mAppDatabase.updateGratitudeItem(parentListKey, childItemKey, updatedText)
    }

    fun removeGratitudeItem(parentListKey: String, childItemKey: String){
        mAppDatabase.removeGratitudeItem(parentListKey, childItemKey)
    }


    fun clearGratitudeItems(parentListKey: String){
        mAppDatabase.clearGratitudeItems(parentListKey)
    }

    companion object {
        private var instance: GratitudeRepository? = null

        fun getInstance(): GratitudeRepository {
            if (instance == null)
                instance = GratitudeRepository()
            return instance!!
        }
    }
}