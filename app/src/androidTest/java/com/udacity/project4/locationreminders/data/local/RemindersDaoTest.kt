package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

@get:Rule
var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest{
        //Given
        val reminder=ReminderDTO("title","description","location",0.0,0.0)
        database.reminderDao().saveReminder(reminder)
        //When
        val loaded=database.reminderDao().getReminderById(reminder.id)

        //Then
        assertThat(loaded as ReminderDTO,notNullValue())
        assertThat(loaded.id,`is`(reminder.id))
        assertThat(loaded.title,`is`(reminder.title))
        assertThat(loaded.location,`is`(reminder.location))
        assertThat(loaded.latitude,`is`(reminder.latitude))
        assertThat(loaded.longitude,`is`(reminder.longitude))
    }
    @Test
    fun getAllReminders()= runBlockingTest{
        //Given
        val reminder1=ReminderDTO("title1","description1","location1",10.0,10.0)
        val reminder2=ReminderDTO("title2","description2","location2",20.0,20.0)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        //When
        val reminderList=database.reminderDao().getReminders()

        //Then
        assertThat(reminderList.size,`is`(2))
    }

    @Test
    fun deleteAllReminders()= runBlockingTest{
        //Given
        val reminder1=ReminderDTO("title1","description1","location1",10.0,10.0)
        val reminder2=ReminderDTO("title2","description2","location2",20.0,20.0)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().deleteAllReminders()
        //When
        val reminderList=database.reminderDao().getReminders()

        //Then
        assertThat(reminderList.size,`is`(0))
    }
}