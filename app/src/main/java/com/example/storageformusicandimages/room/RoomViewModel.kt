package com.example.storageformusicandimages.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.storageformusicandimages.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val db = App.database
    private val userDao = db.noteDao()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        // Инициализация StateFlow данными из базы при создании ViewModel
        viewModelScope.launch {
            userDao.getAllNotes().collect {
                _users.value = it
            }
        }
    }

    // Добавить нового пользователя
    fun insertUser(name: String, password: String) = viewModelScope.launch {
        userDao.insert(User(content = "$name:$password"))
    }
}

