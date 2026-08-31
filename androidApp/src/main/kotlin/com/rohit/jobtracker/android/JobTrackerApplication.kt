package com.rohit.jobtracker.android

import android.app.Application
import com.rohit.jobtracker.android.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class JobTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@JobTrackerApplication)
            modules(appModule)
        }
    }
}
