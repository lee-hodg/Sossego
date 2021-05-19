package com.example.android.sossego.database.gratitude

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class FirebaseGratitudeItem(
    val gratitudeItemId: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    var gratitudeText: String = "",
)


@IgnoreExtraProperties
data class FirebaseGratitudeList(
    val userId: String = "",
    val gratitudeListId: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val gratitudeItems: Map<String, FirebaseGratitudeItem>? = null
)
