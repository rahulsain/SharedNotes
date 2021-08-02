package com.rahuls.sharednotes.roomdb

import androidx.lifecycle.LiveData

class NotesRepository(private val emailDao: EmailDao) {
    val allEmail: LiveData<List<Email>> = emailDao.getAllEmails()

    suspend fun insert(email: Email){
        emailDao.insert(email)
    }
    suspend fun delete(email: Email){
        emailDao.delete(email)
    }

    suspend fun deleteAll(){
        emailDao.deleteAll()
    }
}