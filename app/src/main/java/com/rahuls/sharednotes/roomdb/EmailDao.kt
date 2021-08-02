package com.rahuls.sharednotes.roomdb

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EmailDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(email: Email)

    @Delete
    suspend fun delete(email: Email)

    @Query("Select * from email_table order by id ASC")
    fun getAllEmails(): LiveData<List<Email>>

    @Query("DELETE FROM email_table")
    suspend fun deleteAll()
}