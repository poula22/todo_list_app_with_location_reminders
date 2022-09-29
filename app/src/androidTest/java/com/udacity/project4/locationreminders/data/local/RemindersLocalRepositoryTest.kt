package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule=MainCoroutineRule()

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        //Given
        localDataSource = RemindersLocalRepository(database.reminderDao(),Dispatchers.Main)
    }

    @Test
    fun getReminders_ReminderDatabaseIsEmpty()=mainCoroutineRule.runBlockingTest{
        //When
        val reminders=(localDataSource.getReminders() as Result.Success).data
        //Then
        assertThat(reminders.size,`is`(0))

    }

    @Test
    fun saveReminder_retrievesReminder()= mainCoroutineRule.runBlockingTest {
        //Given
        val reminder1=ReminderDTO("title1","description1","location1",10.0,10.0)
        val reminder2=ReminderDTO("title2","description2","location2",20.0,20.0)
        localDataSource.saveReminder(reminder1)
        localDataSource.saveReminder(reminder2)

        //When
        val result=(localDataSource.getReminder(reminder1.id) as Result.Success).data

        //Then
        assertThat(reminder1.id,`is`(result.id))
        assertThat(reminder1.longitude,`is`(result.longitude))
        assertThat(reminder1.latitude,`is`(result.latitude))
        assertThat(reminder1.description,`is`(result.description))
        assertThat(reminder1.location,`is`(result.location))
        assertThat(reminder1.title,`is`(result.title))
    }

    @Test
    fun deleteAllReminders()= mainCoroutineRule.runBlockingTest {
        //Given
        val reminder1=ReminderDTO("title1","description1","location1",10.0,10.0)
        val reminder2=ReminderDTO("title2","description2","location2",20.0,20.0)
        localDataSource.saveReminder(reminder1)
        localDataSource.saveReminder(reminder2)
        //When
        localDataSource.deleteAllReminders()
        //Then
        val result=(localDataSource.getReminders() as Result.Success).data
        assertThat(result.size,`is`(0))
    }
}