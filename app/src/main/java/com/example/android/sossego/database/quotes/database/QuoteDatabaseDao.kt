package com.example.android.sossego.database.quotes.database


import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QuoteDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: DatabaseQuote): Long

    @Query("SELECT * from quote_table")
    fun getAll(): LiveData<List<DatabaseQuote>>

    @Query("SELECT * FROM quote_table ORDER BY date DESC LIMIT 1")
    fun getLatest(): LiveData<DatabaseQuote?>

    @Query("SELECT * from quote_table where date(date/1000,'unixepoch') = date('now')  ORDER BY date DESC LIMIT 1")
    fun getTodayRecord(): DatabaseQuote?

}