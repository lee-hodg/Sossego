package com.example.android.sossego.database.gratitude.repository

import com.example.android.sossego.database.AppDatabase
import com.example.android.sossego.database.BaseRepository
import com.google.firebase.database.ValueEventListener

/**
 *  The repository handling database operations
 */
class GratitudeRepository(
    private val mAppDatabase: AppDatabase = AppDatabase.getInstance()) : IGratitudeRepository {

    override fun addGratitudeListValueEventListener(userId: String, valueEventListener: ValueEventListener){
        return mAppDatabase.addGratitudeListValueEventListener(userId, valueEventListener)
    }

    override fun removeGratitudeListValueEventListener(userId: String, valueEventListener: ValueEventListener){
        return mAppDatabase.removeGratitudeListValueEventListener(userId, valueEventListener)
    }

    override fun addGratitudeListDetailValueEventListener(valueEventListener: ValueEventListener,
                                                          childKey: String){
        return mAppDatabase.addGratitudeListDetailValueEventListener(valueEventListener, childKey)
    }

    override fun createGratitudeList(userId: String): String {
        return mAppDatabase.createGratitudeList(userId=userId)
    }

    override fun removeGratitudeList(parentListKey: String) {
        mAppDatabase.removeGratitudeList(parentListKey)
    }

    override fun addGratitudeListItem(parentListId: String, gratitudeItemText: String) {
        return mAppDatabase.addGratitudeItem(parentListId, gratitudeItemText)
    }

    override fun updateGratitudeItem(parentListKey: String, childItemKey: String, updatedText: String){
        mAppDatabase.updateGratitudeItem(parentListKey, childItemKey, updatedText)
    }

    override fun removeGratitudeItem(parentListKey: String, childItemKey: String){
        mAppDatabase.removeGratitudeItem(parentListKey, childItemKey)
    }


    override fun clearGratitudeItems(parentListKey: String){
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