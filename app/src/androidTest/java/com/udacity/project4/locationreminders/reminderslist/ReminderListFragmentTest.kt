package com.udacity.project4.locationreminders.reminderslist


import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

    @get:Rule
    val mainCoroutineRule=MainCoroutineRule()

    @Before
    fun setup() {
        val reminder1 = ReminderDTO("title1","description1","location1",0.0,0.0)
        val reminder2 = ReminderDTO("title2","description2","location2",50.0,50.0)
        val reminder3 = ReminderDTO("title3","description3","location3",100.0,100.0)
        val reminders= mutableListOf(reminder1,reminder2,reminder3)
        fakeDataSource = FakeDataSource(reminders)
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

        stopKoin()

        val module = module {
            single {
                reminderListViewModel
            }
        }


        startKoin {
            modules(module)
        }
    }

    @After
    fun stopKoinAfterTest() {
        stopKoin()
    }


//   test the navigation of the fragments.
    @Test
    fun navigateToSaveReminderFragment() = runBlockingTest {
        // When
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
        //Then
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }


    //    test the displayed data on the UI.
    @Test
    fun saveNewReminderAndShowRemindersList()= mainCoroutineRule.runBlockingTest {
        // When
        val reminder4 = ReminderDTO("title4","description4","location4",150.0,150.0)
        fakeDataSource.saveReminder(reminder4)
        launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        //Then

        //check if the 4 reminders displayed correctly
        for (i in 1..4){
            onView(withText("title$i"))
                .check(matches(isDisplayed()))

            onView(withText("description$i"))
                .check(matches(isDisplayed()))

            onView(withText("location$i"))
                .check(matches(isDisplayed()))
        }

    }

    @Test
    fun showNoData() = mainCoroutineRule.runBlockingTest {
        //When
        fakeDataSource.deleteAllReminders()
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //Then
        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
    }

    //    add testing for the error messages.

    @Test
    fun errorSnackBack() = mainCoroutineRule.runBlockingTest {
        //When
        fakeDataSource.setReturnError(true)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //Then
        onView(withId(R.id.snackbar_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.snackbar_text))
            .check(matches(withText("Test Exception")))
    }

}