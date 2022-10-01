package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(var reminders:MutableList<ReminderDTO>?= mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders():Result<List<ReminderDTO>> {
        if(shouldReturnError){
            return Result.Error("Test Exception")
        }
        reminders?.let { return Result.Success(it) }
        return Result.Error("reminders not found")
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