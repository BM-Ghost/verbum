package com.verbum.app

import android.app.Application
import com.verbum.app.bootstrap.DataBootstrapCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class VerbumApplication : Application() {

    @Inject
    lateinit var dataBootstrapCoordinator: DataBootstrapCoordinator

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        dataBootstrapCoordinator.bootstrap()
    }
}
