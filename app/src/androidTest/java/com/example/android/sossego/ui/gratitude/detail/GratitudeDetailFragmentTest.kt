package com.example.android.sossego.ui.gratitude.detail

import android.app.Application
import android.util.Log
import android.view.KeyEvent

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
import com.example.android.sossego.database.gratitude.FirebaseGratitudeItem
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository
import com.example.android.sossego.database.journal.repository.JournalRepository
import com.example.android.sossego.database.user.repository.UserRepository
import com.example.android.sossego.ui.CustomAssertions.Companion.hasItemCount
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
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito
import java.lang.AssertionError
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
 * to list root of gratitude_lists for example
 *
 **/
@LargeTest
@RunWith(AndroidJUnit4::class)
class GratitudeDetailFragmentTest: AutoCloseKoinTest() {

    private lateinit var gratitudeRepository: GratitudeRepository
    private var gratitudeListKey: String = "SOME-KEY"
    private lateinit var appContext: Application
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private val email = "testuser@gmail.com"
    private val password = "x7yer3232!ss"

    private var allCleared = false
    private var writeSucceeded = false

    companion object{
       private const val TAG = "GratDetailTest"
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
                GratitudeRepository.getInstance()
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
            gratitudeRepository = GratitudeRepository()
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
        firebaseDatabase.reference.child("gratitude_lists").orderByChild("userId")
            .equalTo(firebaseAuth.currentUser!!.uid).get().addOnCompleteListener{task ->

                if(task.isSuccessful) {
                    val children = task.result!!.children
                    children.forEach {

                        val gratitudeListId =
                            it.getValue<FirebaseGratitudeList>()!!.gratitudeListId

                        Log.d(TAG, "[cleanup prep] Delete list w/ id $gratitudeListId")
                        firebaseDatabase.reference.child("gratitude_lists")
                            .child(gratitudeListId)
                            .setValue(null)

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
            Log.e(TAG, "Didn't clear lists...Exit")
            return
        }


        // Now create item
        Log.d(TAG, "begin write block")
        val writeSignal = CountDownLatch(1)
        gratitudeListKey = firebaseDatabase.reference.child("gratitude_items").push().key!!
        Log.d(TAG, "Push generated key $gratitudeListKey")
        Log.d(TAG, "make gratitudeList w/ key $gratitudeListKey and userId ${firebaseAuth.currentUser!!.uid}")
        val gratitudeList = FirebaseGratitudeList(
            gratitudeListId = gratitudeListKey,
            userId = firebaseAuth.currentUser!!.uid,
            createdDate = System.currentTimeMillis(),
            gratitudeItems = null)
        firebaseDatabase.reference.child("gratitude_lists")
            .child(gratitudeListKey).setValue(gratitudeList).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Created gratitude list w/ key $gratitudeListKey. Release latch")
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

    private fun addItem(gratitudeText: String): Boolean {
        Log.d(TAG, "begin item write block")
        val itemWriteSignal = CountDownLatch(1)

        val childItemKey = firebaseDatabase.reference.child("gratitude_lists")
            .child(gratitudeListKey).child("gratitudeItems").push().key
        val firebaseGratitudeItem = FirebaseGratitudeItem(gratitudeText=gratitudeText,
            gratitudeItemId = childItemKey!!)
        firebaseDatabase.reference.child("gratitude_lists").child(gratitudeListKey)
            .child("gratitudeItems").child(childItemKey).setValue(firebaseGratitudeItem).
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Created gratitude list item w/ key $gratitudeListKey and $childItemKey. Release latch")
                    itemWriteSignal.countDown()
                } else {
                    Log.e(TAG, "data creation failed with ${task.exception}")
                }
            }
        Log.d(TAG, "Await item write signal")
        return itemWriteSignal.await(10, TimeUnit.SECONDS)

    }


    @ExperimentalCoroutinesApi
    @Test
    fun newGratitudeList_displaysZeroItems() = runBlockingTest {
            if(!writeSucceeded) {
                Log.e(TAG, "Setup must have failed. Exit")
                return@runBlockingTest
            }


            // the below seems to run on ui thread anyway
            Log.d(TAG, "Try to create bundle with $gratitudeListKey")
            val bundle = bundleOf("gratitudeListIdKey" to gratitudeListKey)
            val scenario = launchFragmentInContainer<GratitudeDetailFragment>(
                bundle,
                R.style.Theme_Sossego
            )
            dataBindingIdlingResource.monitorFragment(scenario)


        onView(withId(R.id.gratitude_detail_recycler))
            .check(hasItemCount(0))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun newGratitudeList_displaysSomeItems() = runBlockingTest {
        if(!writeSucceeded) {
            Log.e(TAG, "Setup must have failed. Exit")
            return@runBlockingTest
        }

        val itemWriteSucceeded1 = addItem("Food")
        val itemWriteSucceeded2 = addItem("Drink")
        val itemWriteSucceeded3 = addItem("Family")

        if(listOf(itemWriteSucceeded1, itemWriteSucceeded2, itemWriteSucceeded3).any{false}){
            Log.e(TAG, "Error when adding items to gratitude list")
            return@runBlockingTest
        }


        // the below seems to run on ui thread anyway
        Log.d(TAG, "Try to create bundle with $gratitudeListKey")
        val bundle = bundleOf("gratitudeListIdKey" to gratitudeListKey)
        val scenario = launchFragmentInContainer<GratitudeDetailFragment>(
            bundle,
            R.style.Theme_Sossego
        )
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(R.id.gratitude_detail_recycler))
            .check(hasItemCount(3))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun newGratitudeList_addItemOnKeyPress() = runBlockingTest {
        if(!writeSucceeded) {
            Log.e(TAG, "Setup must have failed. Exit")
            return@runBlockingTest
        }

        val itemWriteSucceeded = addItem("Food")

        if(!itemWriteSucceeded){
            Log.e(TAG, "Error when adding items to gratitude list")
            return@runBlockingTest
        }


        // the below seems to run on ui thread anyway
        Log.d(TAG, "Try to create bundle with $gratitudeListKey")
        val bundle = bundleOf("gratitudeListIdKey" to gratitudeListKey)
        val scenario = launchFragmentInContainer<GratitudeDetailFragment>(
            bundle,
            R.style.Theme_Sossego
        )
        dataBindingIdlingResource.monitorFragment(scenario)

        // Initially 1
        onView(withId(R.id.gratitude_detail_recycler))
            .check(hasItemCount(1))
        verifyListItemCount(gratitudeListKey, 1)

        // Now let's type and press Add and check we got 2
        onView(withId(R.id.new_gratitude_item)).perform(clearText(),typeText("Kotlin"))
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))

        // Note that we need to use the idling resources or this happens too quickly and espresso
        // doesn't know to wait
        onView(withId(R.id.gratitude_detail_recycler))
            .check(hasItemCount(2))
        verifyListItemCount(gratitudeListKey, 2)


    }

    @ExperimentalCoroutinesApi
    @Test
    fun newGratitudeList_addItemWithButton() = runBlockingTest {
        if(!writeSucceeded) {
            Log.e(TAG, "Setup must have failed. Exit")
            return@runBlockingTest
        }

        val itemWriteSucceeded = addItem("Food")

        if(!itemWriteSucceeded){
            Log.e(TAG, "Error when adding items to gratitude list")
            return@runBlockingTest
        }


        // the below seems to run on ui thread anyway
        Log.d(TAG, "Try to create bundle with $gratitudeListKey")
        val bundle = bundleOf("gratitudeListIdKey" to gratitudeListKey)
        val scenario = launchFragmentInContainer<GratitudeDetailFragment>(
            bundle,
            R.style.Theme_Sossego
        )
        dataBindingIdlingResource.monitorFragment(scenario)

        // Initially 1
        onView(withId(R.id.gratitude_detail_recycler))
            .check(hasItemCount(1))
        verifyListItemCount(gratitudeListKey, 1)


        // Now let's type and press Add and check we got 2
        onView(withId(R.id.new_gratitude_item)).perform(clearText(),typeText("Buttons"))
        // Click the add button
        onView(withId(R.id.add_item_button)).perform(click())
        onView(withId(R.id.new_gratitude_item)).perform(clearText(),typeText("Lists"))
        // Click the add button
        onView(withId(R.id.add_item_button)).perform(click())
        verifyListItemCount(gratitudeListKey, 3)

        // Note that we need to use the idling resources or this happens too quickly and espresso
        // doesn't know to wait
        onView(withId(R.id.gratitude_detail_recycler))
            .check(hasItemCount(3))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun clearButton_RemovesItems() = runBlockingTest {
        if(!writeSucceeded) {
            Log.e(TAG, "Setup must have failed. Exit")
            return@runBlockingTest
        }

        val itemWriteSucceeded1 = addItem("A")
        val itemWriteSucceeded2 = addItem("B")
        val itemWriteSucceeded3 = addItem("C")

        if(listOf(itemWriteSucceeded1, itemWriteSucceeded2, itemWriteSucceeded3).any{false}){
            Log.e(TAG, "Error when adding items to gratitude list")
            return@runBlockingTest
        }


        // the below seems to run on ui thread anyway
        Log.d(TAG, "Try to create bundle with $gratitudeListKey")
        val bundle = bundleOf("gratitudeListIdKey" to gratitudeListKey)
        val scenario = launchFragmentInContainer<GratitudeDetailFragment>(
            bundle,
            R.style.Theme_Sossego
        )
        dataBindingIdlingResource.monitorFragment(scenario)

        // GIVEN- Initially 3
        onView(withId(R.id.gratitude_detail_recycler))
            .check(hasItemCount(3))

        // WHEN -  click clear button
        onView(withId(R.id.gratitude_detail_clear_button)).perform(click())

        // THEN - Should be zero
        onView(withId(R.id.gratitude_detail_recycler))
            .check(hasItemCount(0))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun deleteButton_removesAndNavigates() = runBlockingTest {
        if(!writeSucceeded) {
            Log.e(TAG, "Setup must have failed. Exit")
            return@runBlockingTest
        }

        // the below seems to run on ui thread anyway
        Log.d(TAG, "Try to create bundle with $gratitudeListKey")
        val bundle = bundleOf("gratitudeListIdKey" to gratitudeListKey)
        val scenario = launchFragmentInContainer<GratitudeDetailFragment>(
            bundle,
            R.style.Theme_Sossego
        )
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // GIVEN 1 list
        verify_list_count(1)


        // WHEN -  click delete button
        onView(withId(R.id.gratitude_detail_delete_btn)).perform(click())

        // THEN - nav back to listing
        Mockito.verify(navController).navigate(
            GratitudeDetailFragmentDirections.actionNavigationGratitudeDetailFragmentToHome())

        // and no gratitude lists left
        verify_list_count(0)

    }

    private fun verify_list_count(expectedCount: Int) {
        val waitCountSignal = CountDownLatch(1)
        var gratitudeListCount = 1
        firebaseDatabase.reference.child("gratitude_lists").orderByChild("userId")
            .equalTo(firebaseAuth.currentUser!!.uid).get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val children = task.result!!.children
                    gratitudeListCount = children.count()

                    Log.d(TAG, "waitCountSignal release")
                    waitCountSignal.countDown()
                } else {
                    Log.e(TAG, "Could not count lists")
                }


            }
        Log.d(TAG, "await count Signal")
        if (waitCountSignal.await(10, TimeUnit.SECONDS)) {
            assertThat(gratitudeListCount, `is`(expectedCount))
        } else {
            throw AssertionError("Could not get count to do check")
        }
    }

    private fun verifyListItemCount(parentListKey: String, expectedCount: Int) {
        val waitCountSignal = CountDownLatch(1)
        var listItemCount: Int? = null
        firebaseDatabase.reference.child("gratitude_lists").child(parentListKey).child("gratitudeItems")
            .get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val children = task.result!!.children
                    listItemCount = children.count()

                    Log.d(TAG, "waitCountSignal release")
                    waitCountSignal.countDown()
                } else {
                    Log.e(TAG, "Could not count list items")
                }


            }
        Log.d(TAG, "await count Signal")
        if (waitCountSignal.await(10, TimeUnit.SECONDS)) {
            assertThat(listItemCount, `is`(expectedCount))
        } else {
            throw AssertionError("Could not get count to do check")
        }
    }
}


