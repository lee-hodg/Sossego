package com.example.android.sossego.database.user.repository

import com.example.android.sossego.database.BaseRepository
import com.google.firebase.database.ValueEventListener

/**
 *  The repository handling database operations
 */
class UserRepository private constructor() : BaseRepository() {

    fun createNewUser(uid: String, displayName: String?, email: String?) {
        return mAppDatabase.createNewUser(uid, displayName, email)
    }

    fun createNewLogin(uid: String) {
        mAppDatabase.createNewLogin(uid)
    }

    fun incrementUserStreakCount(uid: String){
        mAppDatabase.incrementUserStreakCount(uid)
    }

    fun addStreakCountListener(valueEventListener: ValueEventListener, userId: String? ){
        return mAppDatabase.addStreakCountListener(valueEventListener, userId)
    }

    fun removeStreakCountListener(valueEventListener: ValueEventListener, userId: String?){
        return mAppDatabase.removeStreakCountListener(valueEventListener, userId)
    }
    companion object {
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            if (instance == null)
                instance = UserRepository()
            return instance!!
        }
    }
}