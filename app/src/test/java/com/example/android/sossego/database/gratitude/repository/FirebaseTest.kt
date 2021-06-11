package com.example.android.sossego.database.gratitude.repository

import com.example.android.sossego.database.AppDatabase

import com.example.android.sossego.ui.gratitude.listing.GratitudeViewModel

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Matchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate


@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(JUnit4::class)
@PrepareForTest(FirebaseDatabase::class)
@PowerMockIgnore("jdk.internal.reflect.*")
class BasicDatabaseTest {

    private var mockedDatabaseReference: DatabaseReference? = null
    private lateinit var gratitudeRepository: GratitudeRepository

//    @get:Rule
//    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        mockedDatabaseReference = Mockito.mock(DatabaseReference::class.java)

        `when`(mockedDatabaseReference!!.child(Matchers.anyString()))
            .thenReturn(mockedDatabaseReference)

        `when`(mockedDatabaseReference?.push())
            .thenReturn(mockedDatabaseReference)

        val mockedFirebaseDatabase = Mockito.mock(FirebaseDatabase::class.java)
        `when`(mockedFirebaseDatabase.reference).thenReturn(mockedDatabaseReference)

        PowerMockito.mockStatic(FirebaseDatabase::class.java)
        `when`(FirebaseDatabase.getInstance()).thenReturn(mockedFirebaseDatabase)

        val appDatabase = AppDatabase.getInstance(mockedFirebaseDatabase)
        gratitudeRepository = GratitudeRepository(appDatabase)
    }

//   private lateinit var gratitudeRepository: GratitudeRepository

    // Executes each task synchronously using Architecture Components.
//    @get:Rule
//    var instantExecutorRule = InstantTaskExecutorRule()

//    @Before
//    fun before(){
//        val fireBaseDatabase = FirebaseDatabase.getInstance()
//        fireBaseDatabase.useEmulator("10.0.2.2", 9000)
//        val appDatabase = AppDatabase(fireBaseDatabase)
//        val gratitudeRepository = GratitudeRepository(appDatabase)
//
//    }
    //when(mockedDataSnapshot.getValue(User.class)).thenReturn(testOrMockedUser)
    //valueEventListener.onCancelled(...);

    // check preferences are updated
    //public fun someTest() = runBlockingTest {
    @Test
    public fun someTest() {


//        Mockito.doAnswer { invocation ->
//            val valueEventListener = invocation.arguments[0] as ValueEventListener
//            val mockedDataSnapshot = Mockito.mock(DataSnapshot::class.java)
//            //when(mockedDataSnapshot.getValue(User.class)).thenReturn(testOrMockedUser)
//            valueEventListener.onDataChange(mockedDataSnapshot)
//            //valueEventListener.onCancelled(...);
//            null
//        }.`when`(mockedDatabaseReference)?.addListenerForSingleValueEvent(
//            Matchers.any(
//                ValueEventListener::class.java
//            )
//        )

        //val gratitudeViewModel = GratitudeViewModel(gratitudeRepository)

        `when`(mockedDatabaseReference?.key)
            .thenReturn("some-key")

        val listKey = gratitudeRepository.createGratitudeList(userId = "xxxx")
        assertThat(
            listKey,
            `is`("some-key")
        )

//        gratitudeViewModel.addNewGratitudeList()
//
//        val value = gratitudeViewModel.navigateToGratitudeListDetail.getOrAwaitValue()
//
//        assertThat(
//            value,
//            `is`("some-key")
//        )


    }
}