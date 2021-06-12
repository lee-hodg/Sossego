package com.example.android.sossego.database.gratitude.repository

import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.google.firebase.database.ValueEventListener
import java.util.*

class FakeTestGratitudeRepository(
    public var gratitudeLists: MutableList<FirebaseGratitudeList>? = mutableListOf()): IGratitudeRepository {
    override fun addGratitudeListValueEventListener(
        userId: String,
        valueEventListener: ValueEventListener
    ) {
        TODO("Not yet implemented")
    }

    override fun removeGratitudeListValueEventListener(
        userId: String,
        valueEventListener: ValueEventListener
    ) {
        TODO("Not yet implemented")
    }

    override fun addGratitudeListDetailValueEventListener(
        valueEventListener: ValueEventListener,
        childKey: String
    ) {
        TODO("Not yet implemented")
    }

    private fun getUUID(): String{
        val uuid: UUID = UUID.randomUUID()
        return uuid.toString()
    }

    override fun createGratitudeList(userId: String): String {
        val newListId = getUUID()
        gratitudeLists?.add(
            FirebaseGratitudeList(userId=userId,
                gratitudeListId=newListId)
        )
        return newListId
    }

    override fun clearGratitudeItems(parentListKey: String) {
        gratitudeLists?.clear()
    }

    override fun removeGratitudeList(parentListKey: String) {
        TODO("Not yet implemented")
    }

    override fun addGratitudeListItem(parentListId: String, gratitudeItemText: String) {
        TODO("Not yet implemented")
    }

    override fun updateGratitudeItem(
        parentListKey: String,
        childItemKey: String,
        updatedText: String
    ) {
        TODO("Not yet implemented")
    }

    override fun removeGratitudeItem(parentListKey: String, childItemKey: String) {
        TODO("Not yet implemented")
    }


}