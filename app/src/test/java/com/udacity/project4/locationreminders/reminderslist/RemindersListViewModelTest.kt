package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    /* here we will test loadReminder function in 3 cases:
    -reminder list is empty
    -reminder list is not empty
    */

    private lateinit var fakeDataSource: FakeDataSource
    //subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()



    @Before
    fun setupViewModel(){
        val reminder1 = ReminderDTO("title1","description1","location1",0.0,0.0)
        val reminder2 = ReminderDTO("title2","description2","location2",50.0,50.0)
        val reminder3 = ReminderDTO("title3","description3","location3",100.0,100.0)
        val reminder4 = ReminderDTO("title4","description4","location4",150.0,150.0)
        val reminders= mutableListOf(reminder1,reminder2,reminder3,reminder4)
        fakeDataSource=FakeDataSource(reminders)
        //Given
        remindersListViewModel=RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

    }

    @After
    fun stopKoinAfterTest() {
        stopKoin()
    }



    @Test
    fun loadReminders_ReminderList(){
        //When
        remindersListViewModel.loadReminders()
        //Then
        val value= remindersListViewModel.remindersList.getOrAwaitValue()
        val flag=remindersListViewModel.showLoading.getOrAwaitValue()
        val reminder=value.get(0)
        assertThat(value.size, (not(0)))
        assertThat(flag, `is`(false))
        assertThat(reminder.title, `is`("title1"))
        assertThat(reminder.description, `is`("description1"))
        assertThat(reminder.location, `is`("location1"))
        assertThat(reminder.latitude, `is`(0.0))
        assertThat(reminder.longitude, `is`(0.0))
    }

    @Test
    fun loadReminders_check_loading_deleteAllReminders_emptyRemindersList()= mainCoroutineRule.runBlockingTest{
        //When
        fakeDataSource.deleteAllReminders()
        this.pauseDispatcher()
        remindersListViewModel.loadReminders()
        var flag=remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(flag, `is`(true))
        this.resumeDispatcher()

        //Then
        val reminderList= remindersListViewModel.remindersList.getOrAwaitValue()
        flag=remindersListViewModel.showLoading.getOrAwaitValue()

        assertThat(reminderList.size, `is`(0))
        assertThat(flag, `is`(false))

    }

//    @Test
//    fun loadReminders_ReminderListIsNull(){
//        //Given
//        fakeDataSource.reminders=null
//        //When
//        remindersListViewModel.loadReminders()
//        //Then
//        val value=remindersListViewModel.showSnackBar.getOrAwaitValue()
//        assertThat(value, `is`("reminders not found"))
//    }


    @Test
    fun loadReminders_shouldReturnError_testError(){
        //Given
        fakeDataSource.setReturnError(true)
        //When
        remindersListViewModel.loadReminders()
        //Then
        val value=remindersListViewModel.showSnackBar.getOrAwaitValue()
        assertThat(value, `is`("Test Exception"))
    }


}