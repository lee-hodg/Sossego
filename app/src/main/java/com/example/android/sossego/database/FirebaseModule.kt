package com.example.android.sossego.database

import com.example.android.sossego.database.gratitude.FirebaseGratitudeItem
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class AppDatabase private constructor() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var dbRootRef: DatabaseReference
    private lateinit var gratitudeListNode: DatabaseReference

    fun createGratitudeList(): String {

        val gratitudeListKey = gratitudeListNode.push().key
        val gratitudeList = FirebaseGratitudeList(gratitudeListId = gratitudeListKey!!,
            createdDate=System.currentTimeMillis(),
            gratitudeItems = null)

        gratitudeListNode.child(gratitudeListKey).setValue(gratitudeList)

        return gratitudeListKey
    }

    fun addGratitudeListValueEventListener(valueEventListener: ValueEventListener) {
        val dateOrderedLists = gratitudeListNode.orderByChild("createdDate")
        dateOrderedLists.addValueEventListener(valueEventListener)
    }

    fun addGratitudeListDetailValueEventListener(valueEventListener: ValueEventListener,
                                                 childKey: String) {
        gratitudeListNode.child(childKey).addValueEventListener(valueEventListener)
    }

    fun removeGratitudeList(parentListKey: String){
        gratitudeListNode.child(parentListKey).removeValue()
    }

    fun addGratitudeItem(parentListKey: String, gratitudeText: String) {
        val childItemKey = gratitudeListNode.child(parentListKey).child("gratitudeItems").push().key
        val firebaseGratitudeItem = FirebaseGratitudeItem(gratitudeText=gratitudeText,
            gratitudeItemId = childItemKey!!)
        gratitudeListNode.child(parentListKey).child("gratitudeItems").child(
            childItemKey).setValue(firebaseGratitudeItem)
    }

    fun updateGratitudeItem(parentListKey: String, childItemKey: String, updatedText: String){
        gratitudeListNode.child(parentListKey).child("gratitudeItems").child(
            childItemKey).child("gratitudeText").setValue(updatedText)
    }

    fun removeGratitudeItem(parentListKey: String, childItemKey: String){
        gratitudeListNode.child(parentListKey).child("gratitudeItems").child(
            childItemKey).removeValue()
    }

    fun clearGratitudeItems(parentListKey: String){
        gratitudeListNode.child(parentListKey).child("gratitudeItems").setValue(null)
    }


    /**
     * Singleton
     */
    companion object {
        private var appDatabase: AppDatabase? = null

        fun getInstance(): AppDatabase {
            synchronized(this) {

                if (appDatabase == null) {
                    appDatabase = AppDatabase()
                    appDatabase!!.database.setPersistenceEnabled(true)
                    appDatabase!!.dbRootRef = appDatabase!!.database.reference
                    appDatabase!!.gratitudeListNode = appDatabase!!.dbRootRef.child("gratitude_lists")
                }
                return appDatabase!!
            }
        }
    }

}