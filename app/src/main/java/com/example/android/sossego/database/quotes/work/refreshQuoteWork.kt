package com.example.android.sossego.database.quotes.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.sossego.database.quotes.database.QuoteDatabase
import com.example.android.sossego.database.quotes.repository.QuotesRepository
import retrofit2.HttpException

class RefreshDataWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    /**
     * A coroutine-friendly method to do your work.
     * Note: In recent work version upgrade, 1.0.0-alpha12 and onwards have a breaking change.
     * The doWork() function now returns Result instead of Payload because they have combined Payload into Result.
     * Read more here - https://developer.android.com/jetpack/androidx/releases/work#1.0.0-alpha12
     */
    override suspend fun doWork(): Result {
        val database = QuoteDatabase.getInstance(applicationContext)
        val repository = QuotesRepository(database)
        return try {
            repository.refreshQuotes()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}