package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    /*
        here we will test entered data validations
        when location or title is null
    */
    private lateinit var fakeDataSource: FakeDataSource
    //subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()



    @Before
    fun setupViewModel(){
        fakeDataSource= FakeDataSource()
        //Given
        saveReminderViewModel=
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }

    @After
    fun stopKoinAfterTest() {
        stopKoin()
    }

    @Test
    fun validateAndSaveReminder_shouldReturnError_reminderTitleIsNull(){
        //When
        val reminder=ReminderDataItem(null,"desc","location",0.0,0.0)
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //Then
        val showSnackBar=saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(showSnackBar,`is`(R.string.err_enter_title))
        assertThat(fakeDataSource.reminders?.size,`is`(0))
    }



    @Test
    fun validateAndSaveReminder_shouldReturnError_reminderTitleIsEmpty(){
        //When
        val reminder=ReminderDataItem("","desc","location",0.0,0.0)
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //Then
        val showSnackBar=saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(showSnackBar,`is`(R.string.err_enter_title))
        assertThat(fakeDataSource.reminders?.size,`is`(0))
    }

    @Test
    fun validateAndSaveReminder_shouldReturnError_reminderLocationIsNull(){
        //When
        val reminder=ReminderDataItem("title","desc",null,0.0,0.0)
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //Then
        val showSnackBar=saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(showSnackBar,`is`(R.string.err_select_location))
        assertThat(fakeDataSource.reminders?.size,`is`(0))
    }

    @Test
    fun validateAndSaveReminder_shouldReturnError_reminderLocationIsEmpty(){
        //When
        val reminder=ReminderDataItem("title","desc","",0.0,0.0)
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //Then
        val showSnackBar=saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(showSnackBar,`is`(R.string.err_select_location))
        assertThat(fakeDataSource.reminders?.size,`is`(0))
    }


    @Test
    fun validateAndSaveReminder_check_loading_correctReminderData(){
        //When
        val reminder=ReminderDataItem("title","desc","location",0.0,0.0)
        mainCoroutineRule.dispatcher.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //Then
        var showLoading=saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoading,`is`(true))

        mainCoroutineRule.dispatcher.resumeDispatcher()
        showLoading=saveReminderViewModel.showLoading.getOrAwaitValue()
        val showToast=saveReminderViewModel.showToast.getOrAwaitValue()
        val navigationCommand=saveReminderViewModel.navigationCommand.getOrAwaitValue()

        assertThat(showLoading,`is`(false))
        assertThat(showToast,`is`(saveReminderViewModel.app.getString(R.string.reminder_saved)))
        assertThat(navigationCommand,`is`((NavigationCommand.Back) as NavigationCommand))
        assertThat(fakeDataSource.reminders?.size,`is`(1))
    }

}