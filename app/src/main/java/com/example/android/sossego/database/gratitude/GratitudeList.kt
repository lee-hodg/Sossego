package com.example.android.sossego.database.gratitude

import androidx.room.*

/*
See here https://developer.android.com/training/data-storage/room/relationships
 */
@Entity(tableName = "gratitude_list_table")
data class GratitudeList(
    @PrimaryKey(autoGenerate = true)
    var gratitudeListId: Long = 0L,

    @ColumnInfo(name = "created_date")
    val createdDate: Long = System.currentTimeMillis(),

    val elementCount: Int = 0
)


data class GratitudeListWithItems(
    @Embedded val gratitudeList: GratitudeList,
    @Relation(
        parentColumn = "gratitudeListId",
        entityColumn = "parentListId"
    )
    val items: List<GratitudeItem>
)