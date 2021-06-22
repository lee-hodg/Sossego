package com.example.android.sossego.ui.journal.detail



import android.app.Application
import android.util.Log

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.sossego.EspressoIdlingResource
import com.example.android.sossego.R
import com.example.android.sossego.database.journal.FirebaseJournalEntry

import com.example.android.sossego.database.journal.repository.JournalRepository
import com.example.android.sossego.database.user.repository.UserRepository
import com.example.android.sossego.ui.login.LoginViewModel
import com.example.android.sossego.ui.util.DataBindingIdlingResource
import com.example.android.sossego.ui.util.monitorFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.hamcrest.Matchers.`is`
import org.mockito.Mockito

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instead of building our own Service Locator or constructor dependency injection
 * We can use a library like Koin to just inject the dependencies, and switch them out
 * in the tests to test/fake versions as follows.
 *
 * Follow here (https://medium.com/inspiredbrilliance/getting-started-with-firebase-emulators-suite-for-android-70c1bf87ffe)
 * to install the Firebase emulator:
 *
 * 1/ npm install -g firebase-tools
 * 2/ firebase login
 * 3/ select project
 * 4/ firebase init
 *
 * Run it w/ firebase emulators:start
 * and manage it locally here http://localhost:4000/
 *
 * In the Before section, we stop Koin and then inject emulator versions of these Firebase
 * dependencies (this proxies from android emulator to host machine, get ports from the console)
 *
 *         firebaseDatabase.useEmulator("10.0.2.2", 9000)
 *         firebaseAuth.useEmulator("10.0.2.2", 9099)
 *
 * On production Koin will set up dependencies without the emulator, so we get real db.
 *
 *  You also need to set cleartext true in android manifest and network_security_config.xml
 *  in res/xml to specify the 10.0.2.2 domain
 *
 *  Edit firebase rules in the emulator project (~/firebaseproject) dir
 *  I added ".write": "(auth !== null && query.orderByChild == 'userId' && query.equalTo == auth.uid)",
 * to list root of journal_entries for example
 *
 **/
@LargeTest
@RunWith(AndroidJUnit4::class)
class JournalEntryDetailFragmentTest: AutoCloseKoinTest() {

    private lateinit var journalRepository: JournalRepository
    private var journalListKey: String = "SOME-KEY"
    private lateinit var appContext: Application
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private val email = "testuser@gmail.com"
    private val password = "x7yer3232!ss"

    private var allCleared = false
    private var writeSucceeded = false

    companion object{
        private const val TAG = "JournalDetailTest"
    }
    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     * Want to set up the auth and database to use local firebase emulator
     * Also remember to turn off animations in the android emulator
     */
    @ExperimentalCoroutinesApi
    @Before
    fun init() {

        stopKoin()//stop the original app koin

        appContext = ApplicationProvider.getApplicationContext()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.useEmulator("10.0.2.2", 9000)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.useEmulator("10.0.2.2", 9099)
        val coroutineDispatcher: CoroutineDispatcher = TestCoroutineDispatcher()

        val myModule = module(override = true) {

            single { firebaseDatabase }
            single { firebaseAuth }
            single {
                JournalRepository.getInstance()
            }
            single {
                JournalRepository.getInstance()
            }
            single {
                UserRepository.getInstance()
            }
            single {
                LoginViewModel()
            }
            single {
                coroutineDispatcher
            }
        }

        // start Koin!
        startKoin {
            // declare used Android context
            androidContext(appContext)
            // declare modules
            modules(listOf(myModule))
        }

        //clear the data to start fresh
        runBlocking {
            // Clear all (the problem is that this fails with perm denied)
            // firebaseDatabase.reference.setValue(null)
            // Now Koin is started get the ref to repo
            journalRepository = JournalRepository()
            // ensure we have a user created
            firebaseAuth.createUserWithEmailAndPassword(email, password)

            // If we stick to just this user we can sign in as him and clear all his lists
            dataSetup()


        }
    }

    private fun dataSetup() {
        /**
         * The countdown latch mechanism let's us await on an asynchronous result.
         * If count=1 it's like a simple wait/resume switch.
         * The problem is even if we use runBlockingTest so things are supposed to be synchronous
         * Firebase likely just explicitly switches the dispatcher (probably to Dispatcher.IO)
         * Even if we use the MainCoroutineRule rule (like for local tests that don't actually have
         * a main looper) to setMain to TestCoroutineDispatcher, this won't help if the code
         * under test doesn't even use the Dispatcher.Main (remember this was useful in local tests
         * only because ViewModelScope uses Main dispatcher by default). If code under text
         * switched the dispatcher to IO we'd have to dependency inject that dispatcher to the view
         * so that we could switch it out during testing to TestCoroutineDispatcher.
         *
         * We can't do that for Firebase code (as far as I know) so instead use the CountDownLatch
         * to force waiting on those callbacks
         */
        Log.d(TAG,"Begin dataSetup")

        // Auth first
        val authSignal = CountDownLatch(1)
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                Log.d(TAG,"Release auth latch. Signed in as ${task.result!!.user!!.uid}.")
                authSignal.countDown()
            }
            else{
                Log.e(TAG, "Could not auth")
            }
        }
        Log.d(TAG, "authSignal await")
        authSignal.await(10, TimeUnit.SECONDS)

        // Block to clear
        Log.d(TAG, "begin clear block for user ${firebaseAuth.currentUser!!.uid}")
        val clearSignal = CountDownLatch(1)
        firebaseDatabase.reference.child("journal_entries").orderByChild("userId")
            .equalTo(firebaseAuth.currentUser!!.uid).get().addOnCompleteListener{task ->

                if(task.isSuccessful) {
                    val children = task.result!!.children
                    children.forEach {

                        val journalEntryId =
                            it.getValue<FirebaseJournalEntry>()!!.journalEntryId

                        Log.d(TAG, "[cleanup prep] Delete list w/ id $journalEntryId")
                        if (journalEntryId != null) {
                            firebaseDatabase.reference.child("journal_entries")
                                .child(journalEntryId)
                                .setValue(null)
                        }

                    }
                    // Note the actual deletion may happen later but it is staged now
                    Log.d(TAG, "allDeletedLatch clearSignal release")
                    clearSignal.countDown()
                }
                else{
                    Log.e(TAG, "Could not fetch lists")
                }


            }
        Log.d(TAG, "await clear Signal")
        allCleared = clearSignal.await(10, TimeUnit.SECONDS)

        if(!allCleared){
            // note this can happen on first run when key doesn't exist for journal_entries
            Log.e(TAG, "Didn't clear lists...")
        }


        // Now create item
        Log.d(TAG, "begin write block")
        val writeSignal = CountDownLatch(1)
        journalListKey = firebaseDatabase.reference.child("journal_entries").push().key!!
        Log.d(TAG, "Push generated key $journalListKey")
        Log.d(TAG, "make journalList w/ key $journalListKey and userId ${firebaseAuth.currentUser!!.uid}")
        val journalList = FirebaseJournalEntry(
            journalEntryId = journalListKey,
            userId = firebaseAuth.currentUser!!.uid,
            createdDate = System.currentTimeMillis(),
            entryText = "Dear diary...")
        firebaseDatabase.reference.child("journal_entries")
            .child(journalListKey).setValue(journalList).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Created journal list w/ key $journalListKey. Release latch")
                    writeSignal.countDown()
                } else {
                    Log.e(TAG, "data creation failed with ${task.exception}")
                }
            }
        Log.d(TAG, "Await write signal")
        writeSucceeded = writeSignal.await(10, TimeUnit.SECONDS)
    }

    @After
    fun stop() {
        stopKoin()
    }
    
    @ExperimentalCoroutinesApi
    @Test
    fun newJournalEntry_displaysEmptyText() = runBlockingTest {
        if(!writeSucceeded) {
            Log.e(TAG, "Setup must have failed. Exit")
            return@runBlockingTest
        }

        // the below seems to run on ui thread anyway
        Log.d(TAG, "Try to create bundle with $journalListKey")
        val bundle = bundleOf("journalEntryIdKey" to journalListKey)
        val scenario = launchFragmentInContainer<JournalEntryDetailFragment>(
            bundle,
            R.style.Theme_Sossego
        )
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(R.id.journal_entry_edit_text)).check(matches(withSubstring("Dear diary")))
    }




    @ExperimentalCoroutinesApi
    @Test
    fun deleteButton_removesAndNavigates() = runBlockingTest {
        if(!writeSucceeded) {
            Log.e(TAG, "Setup must have failed. Exit")
            return@runBlockingTest
        }

        // the below seems to run on ui thread anyway
        Log.d(TAG, "Try to create bundle with $journalListKey")
        val bundle = bundleOf("journalEntryIdKey" to journalListKey)
        val scenario = launchFragmentInContainer<JournalEntryDetailFragment>(
            bundle,
            R.style.Theme_Sossego
        )
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // GIVEN 1 journal entry
        verify_journal_count(1)


        // WHEN -  click delete button
        onView(withId(R.id.journal_entry_detail_delete_btn)).perform(click())

        // THEN - nav back to listing
        Mockito.verify(navController).navigate(
            JournalEntryDetailFragmentDirections.actionJournalEntryDetailFragment2ToNavigationJournal())

        // and no journals left
        verify_journal_count(0)

    }

    private fun verify_journal_count(expectedCount: Int) {
        val waitCountSignal = CountDownLatch(1)
        var journalCount = 1
        firebaseDatabase.reference.child("journal_entries").orderByChild("userId")
            .equalTo(firebaseAuth.currentUser!!.uid).get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val children = task.result!!.children
                    journalCount = children.count()

                    Log.d(TAG, "waitCountSignal release")
                    waitCountSignal.countDown()
                } else {
                    Log.e(TAG, "Could not count journals")
                }


            }
        Log.d(TAG, "await count Signal")
        if (waitCountSignal.await(10, TimeUnit.SECONDS)) {
            assertThat(journalCount, `is`(expectedCount))
        } else {
            throw AssertionError("Could not get count to do check")
        }
    }

}


