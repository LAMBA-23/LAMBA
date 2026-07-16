package com.lamba.app

import android.app.Application

class LambaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySavedTheme(this)
    }
}
