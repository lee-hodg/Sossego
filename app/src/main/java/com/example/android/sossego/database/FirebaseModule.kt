package com.example.android.sossego.database

import com.example.android.sossego.database.gratitude.FirebaseGratitudeItem
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.example.android.sossego.database.journal.FirebaseJournalEntry
import com.example.android.sossego.database.user.repository.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber


class AppDatabase private constructor() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var dbRootRef: DatabaseReference
    private lateinit var gratitudeListNode: DatabaseReference
    private lateinit var journalEntryNode: DatabaseReference
    private lateinit var userNode: DatabaseReference

    fun createGratitudeList(userId: String): String {

        val gratitudeListKey = gratitudeListNode.push().key
        val gratitudeList = FirebaseGratitudeList(gratitudeListId = gratitudeListKey!!,
            userId=userId,
            createdDate=System.currentTimeMillis(),
            gratitudeItems = null)

        gratitudeListNode.child(gratitudeListKey).setValue(gratitudeList)

        return gratitudeListKey
    }

    fun addGratitudeListValueEventListener(userId: String, valueEventListener: ValueEventListener) {
        Timber.tag(TAG).d("Add gratitude list value event listener for userId $userId")
        val userFilteredGratitudeLists = gratitudeListNode.orderByChild("userId").equalTo(userId)
        userFilteredGratitudeLists.addValueEventListener(valueEventListener)
    }

    fun removeGratitudeListValueEventListener(userId: String, valueEventListener: ValueEventListener) {
        Timber.tag(TAG).d("Add gratitude list value event listener for userId $userId")
        val userFilteredGratitudeLists = gratitudeListNode.orderByChild("userId").equalTo(userId)
        userFilteredGratitudeLists.removeEventListener(valueEventListener)
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
     * Deal with journal entries
     */
    fun addJournalEntryListValueEventListener(userId: String, valueEventListener: ValueEventListener) {
        Timber.tag(TAG).d("Add journal entry list value event listener for userId $userId")
        val userFilteredJournals = journalEntryNode.orderByChild("userId").equalTo(userId)
        userFilteredJournals.addValueEventListener(valueEventListener)
    }

    fun removeJournalEntryListValueEventListener(userId: String, valueEventListener: ValueEventListener) {
        Timber.tag(TAG).d("Remove journal entry list value event listener for userId $userId")
        val userFilteredJournals = journalEntryNode.orderByChild("userId").equalTo(userId)
        userFilteredJournals.removeEventListener(valueEventListener)
    }


    fun createJournalEntry(userId: String): String {
        val journalEntryKey = journalEntryNode.push().key
        val journalEntry = FirebaseJournalEntry(journalEntryId=journalEntryKey,
            createdDate=System.currentTimeMillis(),
            userId=userId,
            entryText = ""
        )
        journalEntryNode.child(journalEntryKey!!).setValue(journalEntry)
        return journalEntryKey
    }

    fun removeJournalEntry(journalEntryKey: String){
        Timber.tag(TAG).d("removeJournalEntry with key $journalEntryKey")
        journalEntryNode.child(journalEntryKey).removeValue()
    }

    fun updateJournalEntry(journalEntryKey: String, updatedText: String){
        journalEntryNode.child(journalEntryKey).get().addOnSuccessListener {
            if(it.exists()) {
                Timber.tag(TAG).d("updateJournalEntry fetched with $journalEntryKey exists. Update...")
                journalEntryNode.child(journalEntryKey).child("entryText").setValue(updatedText)
            }
        }.addOnFailureListener{
            Timber.tag(TAG).e("updateJournalEntry fail fetching with $journalEntryKey")
        }
    }

    fun addJournalEntryDetailValueEventListener(valueEventListener: ValueEventListener,
                                                journalEntryKey: String) {
        journalEntryNode.child(journalEntryKey).addValueEventListener(valueEventListener)
    }

    fun removeJournalEntryDetailValueEventListener(valueEventListener: ValueEventListener,
                                                   journalEntryKey: String){
        journalEntryNode.child(journalEntryKey).removeEventListener(valueEventListener)
    }

    /**
     * Handle user creation in the db
     */
    fun createNewUser(userId: String, displayName: String?, email: String?) {
        val firebaseUser = User(uid=userId, displayName=displayName, email=email)
        userNode.child(userId).setValue(firebaseUser)
    }

    /**
     * Singleton
     */
    companion object {
        const val TAG = "AppDatabase"

        private var appDatabase: AppDatabase? = null

        fun getInstance(): AppDatabase {
            synchronized(this) {

                if (appDatabase == null) {
                    appDatabase = AppDatabase()
                    appDatabase!!.database.setPersistenceEnabled(true)
                    appDatabase!!.dbRootRef = appDatabase!!.database.reference
                    appDatabase!!.gratitudeListNode = appDatabase!!.dbRootRef.child("gratitude_lists")
                    appDatabase!!.journalEntryNode = appDatabase!!.dbRootRef.child("journal_entries")
                    appDatabase!!.userNode = appDatabase!!.dbRootRef.child("users")
                }
                return appDatabase!!
            }
        }
    }

}