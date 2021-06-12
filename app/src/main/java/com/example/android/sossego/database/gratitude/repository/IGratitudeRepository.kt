package com.example.android.sossego.database.gratitude.repository

import com.google.firebase.database.ValueEventListener

interface IGratitudeRepository {
    fun addGratitudeListValueEventListener(userId: String, valueEventListener: ValueEventListener)
    fun removeGratitudeListValueEventListener(
        userId: String,
        valueEventListener: ValueEventListener
    )

    fun addGratitudeListDetailValueEventListener(
        valueEventListener: ValueEventListener,
        childKey: String
    )

    fun createGratitudeList(userId: String): String
    fun removeGratitudeList(parentListKey: String)
    fun addGratitudeListItem(parentListId: String, gratitudeItemText: String)
    fun updateGratitudeItem(parentListKey: String, childItemKey: String, updatedText: String)
    fun removeGratitudeItem(parentListKey: String, childItemKey: String)
    fun clearGratitudeItems(parentListKey: String)
}