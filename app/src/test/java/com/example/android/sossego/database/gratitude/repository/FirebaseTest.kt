package com.example.android.sossego.database.gratitude.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.sossego.MainCoroutineRule
import com.example.android.sossego.database.AppDatabase
import com.example.android.sossego.getOrAwaitValue

import com.example.android.sossego.ui.gratitude.listing.GratitudeViewModel

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate


@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(MockitoJUnitRunner::class) //this line allows you to use the powermock runner and mockito runner
@PrepareForTest(FirebaseDatabase::class)
@PowerMockIgnore("jdk.internal.reflect.*")
class BasicDatabaseTest {

    private var mockedDatabaseReference: DatabaseReference? = null
    private lateinit var gratitudeRepository: GratitudeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        mockedDatabaseReference = Mockito.mock(DatabaseReference::class.java)

        // Child/push just gives another mock ref
        `when`(mockedDatabaseReference!!.child(ArgumentMatchers.anyString()))
            .thenReturn(mockedDatabaseReference)
        `when`(mockedDatabaseReference?.push())
            .thenReturn(mockedDatabaseReference)

        // Mock the FirebaseDatabase and ensure its reference returns the mock ref
        val mockedFirebaseDatabase = Mockito.mock(FirebaseDatabase::class.java)
        `when`(mockedFirebaseDatabase.reference).thenReturn(mockedDatabaseReference)

        // The static mock to ensure getInstance() always means the mocked one
        PowerMockito.mockStatic(FirebaseDatabase::class.java)
        `when`(FirebaseDatabase.getInstance()).thenReturn(mockedFirebaseDatabase)

        // Our AppDatabase to always use this mockedFirebaseDatabase
        val appDatabase = AppDatabase.getInstance(mockedFirebaseDatabase)
        gratitudeRepository = GratitudeRepository(appDatabase)
    }


    @ExperimentalCoroutinesApi
    @Test
    fun addGratitudeList_NavigatesToCorrectDetail() = mainCoroutineRule.runBlockingTest {


        // GIVEN - Our mocked firebase ref push() produces NEW-GRATITUDE-ID as the key
        // We need to set the user id on the model as the add method will use it
        `when`(mockedDatabaseReference?.key)
            .thenReturn("NEW-GRATITUDE-ID")
        val gratitudeViewModel = GratitudeViewModel(gratitudeRepository)
        gratitudeViewModel.setAuthenticatedUserId("SOME-USER-ID")

        // WHEN - We add a new list
        gratitudeViewModel.addNewGratitudeList()

        // THEN - We navigate to detail fragment with the id of the list we just added
        val value = gratitudeViewModel.navigateToGratitudeListDetail.getOrAwaitValue()
        assertThat(
            value,
            `is`("NEW-GRATITUDE-ID")
        )

    }
}