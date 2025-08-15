package com.asensiodev.core.network.init

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ApiKeyStartupInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint =
            EntryPointAccessors.fromApplication(
                context,
                ApiKeyInitializerEntryPoint::class.java,
            )
        val refresher: ApiKeyRefresher = entryPoint.apiKeyRefresher()

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            withTimeoutOrNull(STARTUP_TIMEOUT_MS) {
                refresher.ensureKeyUpToDate()
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

private const val STARTUP_TIMEOUT_MS = 3_000L
