package com.rahuls.sharednotes.roomdb

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    val allEmail: LiveData<List<Email>>
    private val repository: NotesRepository
    init {
        val dao = EmailDatabase.getDatabase(application).getEmailDao()
        repository = NotesRepository(dao)
        allEmail = repository.allEmail

    }

    fun deleteNotes(email: Email) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(email)
    }

    fun insertNotes(email: Email) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(email)
    }

    fun deleteAllNotes() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }
}