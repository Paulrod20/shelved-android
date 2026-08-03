package com.paulrod.shelved

import android.app.Application
import com.paulrod.shelved.appcheck.AppCheckProviderInstaller

class ShelvedApplication : Application() {
    internal val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        AppCheckProviderInstaller.install()
    }
}
