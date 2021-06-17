package com.example.android.sossego.ui.gratitude.detail

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView


import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.sossego.R
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList

import com.example.android.sossego.database.gratitude.repository.GratitudeRepository
import com.example.android.sossego.database.journal.repository.JournalRepository
import com.example.android.sossego.database.user.repository.UserRepository
import com.example.android.sossego.ui.gratitude.listing.GratitudeFragment
import com.example.android.sossego.ui.gratitude.listing.GratitudeFragmentDirections
import com.example.android.sossego.ui.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.not

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
import org.mockito.Mockito.mock
import timber.log.Timber


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
@MediumTest
@RunWith(AndroidJUnit4::class)
class GratitudeListingFragmentTest: AutoCloseKoinTest() {

    private lateinit var gratitudeRepository: GratitudeRepository

    private lateinit var appContext: Application
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private val email = "testuser@gmail.com"
    private val password = "x7yer3232!ss"
    private val displayName = "Bobby Thornton"

    // private val dataBindingIdlingResource = DataBindingIdlingResource()
    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     * Want to set up the auth and database to use local firebase emulator
     * Also remember to turn off animations in the android emulator
     */
    @Before
    fun init() {

        stopKoin()//stop the original app koin

        appContext = ApplicationProvider.getApplicationContext()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.useEmulator("10.0.2.2", 9000)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.useEmulator("10.0.2.2", 9099)
        val myModule = module(override = true) {

            single {firebaseDatabase}
            single {firebaseAuth}
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
            clear_lists()


        }
    }

    private fun clear_lists() {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            firebaseDatabase.reference.child("gratitude_lists").orderByChild("userId")
                .equalTo(firebaseAuth.currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.e(error.toString())
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            val gratitudeListId =
                                it.getValue<FirebaseGratitudeList>()!!.gratitudeListId
                            Timber.d("[cleanup prep] Delete list w/ id $gratitudeListId")
                            firebaseDatabase.reference.child("gratitude_lists")
                                .child(gratitudeListId)
                                .setValue(null)
                        }
                    }
                })
        }

        // ensure to start signed out
        firebaseAuth.signOut()
    }

    @After
    fun stop() {
        stopKoin()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun gratitudeList_quoteShownIfAnonymous() = runBlockingTest {
        // GIVEN
        firebaseAuth.signOut()

        // WHEN - launch gratitude fragment is launched
        launchFragmentInContainer<GratitudeFragment>(Bundle(), R.style.Theme_Sossego)

        // THEN - show quote shown
        onView(withId(R.id.quote_of_the_day_text))
            .check(matches(isDisplayed()))

    }

    @ExperimentalCoroutinesApi
    @Test
    fun gratitudeList_greetingNotShownIfAnonymous() = runBlockingTest {
        // GIVEN
        firebaseAuth.signOut()

        // WHEN - launch gratitude fragment is launched
        launchFragmentInContainer<GratitudeFragment>(Bundle(), R.style.Theme_Sossego)

        // THEN - do not show greeting
        onView(withId(R.id.greeting_tv))
            .check(matches(not(isDisplayed())))

    }

    @ExperimentalCoroutinesApi
    @Test
    fun gratitudeList_promptsLogin() = runBlockingTest {
        // GIVEN
        firebaseAuth.signOut()

        // WHEN - launch gratitude fragment is launched
        launchFragmentInContainer<GratitudeFragment>(Bundle(), R.style.Theme_Sossego)

        // THEN - show login prompt is shown
        onView(withId(R.id.login_tv))
            .check(matches(isDisplayed()))

    }

    @ExperimentalCoroutinesApi
    @Test
    fun gratitudeList_displayNameDisplayed() = runBlockingTest {
        // GIVEN
        firebaseAuth.signInWithEmailAndPassword(email, password)
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName).build()
        firebaseAuth.currentUser?.updateProfile(profileUpdates)

        // WHEN - launch gratitude fragment is launched
        launchFragmentInContainer<GratitudeFragment>(Bundle(), R.style.Theme_Sossego)

        // THEN - show recycler view is shown
        onView(withId(R.id.gratitude_list_recycler))
            .check(matches(isDisplayed()))
        // and the greeting contains the correct display name
        onView(withId(R.id.greeting_tv))
            .check(matches(withSubstring(displayName)))

    }

    @ExperimentalCoroutinesApi
    @Test
    fun gratitudeList_navigatesToDetail() = runBlockingTest {
        // GIVEN
        firebaseAuth.signInWithEmailAndPassword(email, password)
        val scenario = launchFragmentInContainer<GratitudeFragment>(Bundle(), R.style.Theme_Sossego)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - launch gratitude fragment is launched for auth user and fab clicked
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())

        // THEN - verify that detail for this list is going to be navigated to
        // I'd hoped to just use ArgumentMatchers.anyString but it did not work
        firebaseDatabase.reference.child("gratitude_lists").limitToFirst(1).get().addOnSuccessListener {
            val newList = it.getValue<FirebaseGratitudeList>()
            Mockito.verify(navController).navigate(
                GratitudeFragmentDirections.actionNavigationGratitudeToGratitudeDetailFragment(
                    newList!!.gratitudeListId))
        }.addOnFailureListener{
            return@addOnFailureListener
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun gratitudeList_navigatesToItemClickedDetail() = runBlockingTest {
        // GIVEN
        clear_lists()
        firebaseAuth.signInWithEmailAndPassword(email, password)

        val scenario = launchFragmentInContainer<GratitudeFragment>(Bundle(), R.style.Theme_Sossego)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        gratitudeRepository.createGratitudeList(firebaseAuth.currentUser!!.uid)

        // WHEN - launch gratitude fragment is launched for auth user and first item in list clicked
        onView(withId(R.id.gratitude_list_recycler))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
                click()))

        // THEN - verify that detail for this list is going to be navigated to
        // I'd hoped to just use ArgumentMatchers.anyString but it did not work
        firebaseDatabase.reference.child("gratitude_lists").limitToFirst(1).get().addOnSuccessListener {
            val newList = it.getValue<FirebaseGratitudeList>()
            Mockito.verify(navController).navigate(
                GratitudeFragmentDirections.actionNavigationGratitudeToGratitudeDetailFragment(
                    newList!!.gratitudeListId))
        }.addOnFailureListener{
            return@addOnFailureListener
        }
    }
}