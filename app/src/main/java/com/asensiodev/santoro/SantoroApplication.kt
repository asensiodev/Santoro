package com.asensiodev.santoro

import android.app.Application
import com.asensiodev.core.network.init.ApiKeyInitializerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class SantoroApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // TODO(): Use proper injection here
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val entryPoint =
            EntryPointAccessors.fromApplication(
                this,
                ApiKeyInitializerEntryPoint::class.java,
            )

        applicationScope.launch {
            entryPoint.apiKeyInitializer().initialize()
        }
    }
}
