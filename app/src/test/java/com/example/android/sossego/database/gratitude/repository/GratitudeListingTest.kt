package com.example.android.sossego.database.gratitude.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.sossego.MainCoroutineRule
import com.example.android.sossego.getOrAwaitValue

import com.example.android.sossego.ui.gratitude.listing.GratitudeViewModel


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test


/**
 * This way we don't actually mock out Firebase, we just fake the gratitude repo,
 * which is somewhat easier
 */
class GratitudeListingTest {

    private lateinit var gratitudeRepository: FakeTestGratitudeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @ExperimentalCoroutinesApi
    @Test
    fun addGratitudeList_NavigatesToCorrectDetail() = mainCoroutineRule.runBlockingTest {

        // GIVEN
        gratitudeRepository = FakeTestGratitudeRepository()
        val gratitudeViewModel = GratitudeViewModel(gratitudeRepository)
        gratitudeViewModel.setAuthenticatedUserId("SOME-USER-ID")
        assertThat(gratitudeRepository.gratitudeLists?.size , `is`(0))

        // WHEN - We add a new list
        gratitudeViewModel.addNewGratitudeList()

        // THEN - We navigate to detail fragment with the id of the list we just added
        val value = gratitudeViewModel.navigateToGratitudeListDetail.getOrAwaitValue()
        assertThat(
            value,
            notNullValue()
        )
        assertThat(gratitudeRepository.gratitudeLists?.size , `is`(1))

    }
}