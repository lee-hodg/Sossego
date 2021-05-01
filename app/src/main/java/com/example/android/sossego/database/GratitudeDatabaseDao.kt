package com.example.android.sossego.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GratitudeDatabaseDao {

    @Insert
    suspend fun insert(gratitudeList: GratitudeList): Long

    @Insert
    suspend fun insertItem(gratitudeItem: GratitudeItem)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param gratitudeList new value to write
     */
    @Update
    suspend fun update(gratitudeList: GratitudeList)

    /**
     * Selects and returns the row that matches the supplied id
     *
     * @param key gratitudeListId
     */
    @Query("SELECT * from gratitude_list_table WHERE gratitudeListId = :key")
    suspend fun get(key: Long): GratitudeList?


    @Query("SELECT * from gratitude_item_table WHERE gratitudeItemId = :key")
    suspend fun getItem(key: Long): GratitudeItem?

    @Query("SELECT * from gratitude_item_table")
    suspend fun getAllItems(): List<GratitudeItem>

    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM gratitude_list_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by created date in descending order.
     */
    @Query("SELECT * FROM gratitude_list_table ORDER BY created_date DESC")
    fun getAllGratitudeLists(): LiveData<List<GratitudeList>>

    /**
     * Selects and returns the latest gratitude list.
     */
    @Query("SELECT * FROM gratitude_list_table ORDER BY created_date DESC LIMIT 1")
    suspend fun getLatestGratitudeList(): GratitudeList?

    @Transaction
    @Query("SELECT * FROM gratitude_list_table ORDER BY created_date DESC")
    suspend fun getGratitudeListsWithItems(): List<GratitudeListWithItems>
}