package com.asensiodev.santoro

import android.app.Application
import com.asensiodev.core.network.init.ApiKeyInitializerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class SantoroApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val entryPoint =
            EntryPointAccessors.fromApplication(
                this,
                ApiKeyInitializerEntryPoint::class.java,
            )

        runBlocking {
            entryPoint.apiKeyInitializer().initialize()
        }
    }
}
