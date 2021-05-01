package com.example.android.sossego.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*
See here https://developer.android.com/training/data-storage/room/relationships
 */
@Entity(tableName = "gratitude_list_table")
data class GratitudeList(
    @PrimaryKey(autoGenerate = true)
    var gratitudeItemId: Long = 0L,

    @ColumnInfo(name = "created_date")
    val createdDate: Long = System.currentTimeMillis(),
)
