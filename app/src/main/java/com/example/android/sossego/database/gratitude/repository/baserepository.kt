package com.example.android.sossego.database.gratitude.repository

import com.example.android.sossego.database.AppDatabase


abstract class BaseRepository {
    protected val mAppDatabase = AppDatabase.getInstance()
}