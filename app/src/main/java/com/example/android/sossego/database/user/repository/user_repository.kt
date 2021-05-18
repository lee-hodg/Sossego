package com.example.android.sossego.database.user.repository

import com.example.android.sossego.database.BaseRepository

/**
 *  The repository handling database operations
 */
class UserRepository private constructor() : BaseRepository() {

    fun createNewUser(uid: String, displayName: String?, email: String?) {
        return mAppDatabase.createNewUser(uid, displayName, email)
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