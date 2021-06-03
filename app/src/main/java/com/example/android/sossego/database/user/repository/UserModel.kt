package com.example.android.sossego.database.user.repository

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    var displayName: String? = "",
    var email: String? = "",
    var streakCount: Int = 1,
    var lastAppOpenDayOfYear: Int? = null
)

/**
 * Track logins to display streak counts
 **/
@IgnoreExtraProperties
data class UserLogin(
    val uid: String = "",
    val loginDate: Long = System.currentTimeMillis(),
)
