package com.example.android.sossego.database


abstract class BaseRepository {
    protected val mAppDatabase = AppDatabase.getInstance()
}