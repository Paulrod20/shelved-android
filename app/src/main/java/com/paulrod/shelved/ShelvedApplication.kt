package com.paulrod.shelved

import android.app.Application
import com.paulrod.shelved.appcheck.AppCheckProviderInstaller

class ShelvedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCheckProviderInstaller.install()
    }
}
