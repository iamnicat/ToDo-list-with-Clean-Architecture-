package com.nicathaciyev.todoapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nicathaciyev.todoapp.data.models.ToDoData

@Dao
interface ToDoDao {

    @Query("SELECT * FROM todo_table ORDER BY id ASC")
    fun getAllData(): LiveData<List<ToDoData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(toDoData: ToDoData)

}