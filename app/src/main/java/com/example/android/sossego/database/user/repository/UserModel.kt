package com.example.android.sossego.database.user.repository

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    var displayName: String? = "",
    var email: String? = ""
)
