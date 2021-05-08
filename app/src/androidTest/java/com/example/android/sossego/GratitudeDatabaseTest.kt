package com.example.android.sossego

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.sossego.database.*
import com.example.android.sossego.database.gratitude.GratitudeDatabase
import com.example.android.sossego.database.gratitude.GratitudeDatabaseDao
import com.example.android.sossego.database.gratitude.GratitudeItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class)
class GratitudeDatabaseTest {

    private lateinit var gratitudeDao: GratitudeDatabaseDao
    private lateinit var db: GratitudeDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, GratitudeDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        gratitudeDao = db.gratitudeDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetGratitudeList() {
        val gratitudeList = GratitudeList()
        runBlocking {
            gratitudeDao.insert(gratitudeList)
        }
        var latestList: GratitudeList?
        runBlocking {
            latestList = gratitudeDao.getLatestGratitudeList()
        }
        assertNotNull(latestList?.createdDate)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetGratitudeListAndItems() {
        // GIVEN - we have a gratitude list with some child items
        val gratitudeList = GratitudeList()
        var parentId: Long
        runBlocking {
            parentId = gratitudeDao.insert(gratitudeList)
        }
        // Add some entries
        val gratitudeListItem1 = GratitudeItem(gratitudeText="My Health",
            parentListId=parentId)
        val gratitudeListItem2 = GratitudeItem(gratitudeText="My Family",
            parentListId=parentId)
        val gratitudeListItem3 = GratitudeItem(gratitudeText="Food",
            parentListId=parentId)
        runBlocking {
            gratitudeDao.insertItem(gratitudeListItem1)
            gratitudeDao.insertItem(gratitudeListItem2)
            gratitudeDao.insertItem(gratitudeListItem3)

        }

        // WHEN - we get the lists with items
        var gratitudeListWithItems: List<GratitudeListWithItems>
        runBlocking {
            gratitudeListWithItems = gratitudeDao.getGratitudeListsWithItems()

        }

        // THEN - we see 1 list, the correct item count in list
        assertEquals(gratitudeListWithItems.size, 1)
        val firstGratitudeList = gratitudeListWithItems[0]
        assertEquals(firstGratitudeList.items.size, 3)
        assertEquals(firstGratitudeList.items[0].gratitudeText, "My Health")
        assertEquals(firstGratitudeList.items[1].gratitudeText, "My Family")
        assertEquals(firstGratitudeList.items[2].gratitudeText, "Food")
    }
}

