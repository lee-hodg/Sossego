package com.example.android.sossego.ui.gratitude.detail

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer


import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.sossego.R

import com.example.android.sossego.database.gratitude.repository.GratitudeRepository
import com.example.android.sossego.database.journal.repository.JournalRepository
import com.example.android.sossego.database.user.repository.UserRepository
import com.example.android.sossego.ui.gratitude.listing.GratitudeFragment
import com.example.android.sossego.ui.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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


/**
 * Instead of building our own Service Locator or constructor dependency injection
 * We can use a library like Koin to just inject the dependencies, and switch them out
 * in the tests as follows.
 * We then want an instrumented test that adds a new list and check we navigate to its detail
 *
 * Prob I'm going to want to run Firebase emulator, use Koin to ensure I use the emulated one
 * Then I'll need to go through login and should be able to create gratitude lists
 * and so on against the emulator but otherwise continue tests as usual]
 *
 *
 * Notes: You can follow to set up firebase emulator on local machine
 * https://medium.com/inspiredbrilliance/getting-started-with-firebase-emulators-suite-for-android-70c1bf87ffe
 * You then proxy from the android emulator to the local firebase emulators via 10.0.2.2
 * On you local machine visit http://localhost:4000/ to see what is running and what port
 * For testing we need to set
 *  - firebaseDatabase.useEmulator("10.0.2.2", 9000)
 *  - firebaseAuth.useEmulator("10.0.2.2", 9099)
 *  However we do not want to set those things in production, which is where Koin comes in
 *  You also need to set cleartext true in android manifest and network_security_config.xml
 *  in res/xml to specify the 10.0.2.2 domain
 *
 *  For just checking things are up and running  firebaseDatabase.useEmulator("10.0.2.2", 9000)
 * can be set in FirebaseModule and     init{
 * firebaseAuth.useEmulator("10.0.2.2", 9099)} in FirebaseUserLiveData too
 **/

@MediumTest
@RunWith(AndroidJUnit4::class)
class GratitudeDetailFragmentTest: AutoCloseKoinTest() {

    private lateinit var appContext: Application
    private lateinit var firebaseAuth: FirebaseAuth

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
        val firebaseDatabase = FirebaseDatabase.getInstance()
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
            // Ensure signedOut
            //firebaseAuth.signOut()
            //repository.clearGratitudeLists
        }
    }

    @After
    fun stop() {
        stopKoin()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun gratitudeList_promptsLogin() = runBlockingTest {
        // GIVEN
        firebaseAuth.signOut()

        // WHEN - launch gratitude fragment is launched
        launchFragmentInContainer<GratitudeFragment>(Bundle(), R.style.Theme_Sossego)

        // THEN - show login prompt is shown
        Espresso.onView(withId(R.id.login_tv))
            .check(matches(isDisplayed()))

    }

    @ExperimentalCoroutinesApi
    @Test
    fun gratitudeList_displayNameDisplayed() = runBlockingTest {
        // GIVEN
        val email = "testuser@gmail.com"
        val password = "x7yer3232!ss"
        val displayName = "Bobby Thornton"
        firebaseAuth.createUserWithEmailAndPassword(email, password)
        firebaseAuth.signInWithEmailAndPassword(email, password)
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName).build()
        firebaseAuth.currentUser?.updateProfile(profileUpdates)

        // WHEN - launch gratitude fragment is launched
        launchFragmentInContainer<GratitudeFragment>(Bundle(), R.style.Theme_Sossego)

        // THEN - show recycler view is shown
        Espresso.onView(withId(R.id.gratitude_list_recycler))
            .check(matches(isDisplayed()))
        // and the greeting contains the correct display name
        Espresso.onView(withId(R.id.greeting_tv))
            .check(matches(withSubstring(displayName)))

    }
}