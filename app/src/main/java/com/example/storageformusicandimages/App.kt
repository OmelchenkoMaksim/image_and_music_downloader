package com.example.storageformusicandimages

import android.app.Application
import androidx.room.Room
import com.example.storageformusicandimages.room.AppDatabase

class App : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "users.db"
        ).build()
    }
}
