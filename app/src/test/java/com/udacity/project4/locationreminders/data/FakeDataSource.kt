package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders:MutableList<ReminderDTO>?= mutableListOf()) : ReminderDataSource {

    //     Create a fake data source to act as a double to the real data source

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>>  {
        if(shouldReturnError){
            return Result.Error("Test Exception")
        }
        reminders?.let { return Result.Success(it) }
        reminders= mutableListOf()
        return Result.Success(reminders!!)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
            reminders?.add(reminder)
    }


    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError){
            return Result.Error("Test Exception")
        }
        reminders?.forEach {
            if (it.id==id) return Result.Success(it)
        }
        return Result.Error("reminder not found")
    }

    override suspend fun deleteAllReminders() {
            reminders?.clear()
    }


}