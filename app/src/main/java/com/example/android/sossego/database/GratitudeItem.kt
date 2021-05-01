package com.example.android.sossego.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(tableName = "gratitude_item_table",
    foreignKeys = [ForeignKey(
        entity = GratitudeList::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("gratitudeList"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class GratitudeItem(
    @PrimaryKey(autoGenerate = true)
    var gratitudeItemId: Long = 0L,

    @ColumnInfo(name = "gratitude_text")
    var gratitudeText: String = "",

    @ColumnInfo(index = true)
    val gratitudeList: String
)
