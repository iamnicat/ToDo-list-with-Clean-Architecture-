package com.nicathaciyev.todoapp.data.repository

import androidx.lifecycle.LiveData
import com.nicathaciyev.todoapp.data.ToDoDao
import com.nicathaciyev.todoapp.data.models.ToDoData

class ToDoRepository(private val toDoDao: ToDoDao) {
    val getAllData: LiveData<List<ToDoData>> = toDoDao.getAllData()

    suspend fun insertData(toDoData: ToDoData) {
        toDoDao.insertData(toDoData)
    }
}