package com.control.porcinochl

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getDatabase(this)
    }
}