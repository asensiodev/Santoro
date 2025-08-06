package com.asensiodev.core.network.data

import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ApiKeyInitializer
    @Inject
    constructor(
        private val remoteConfigProvider: RemoteConfigProvider,
    ) {
        private var apiKey: String? = null

        suspend fun initialize() {
            val key = remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            check(key.isNotBlank()) { "API key is missing or blank!" }
            apiKey = key
        }

        fun getCachedApiKey(): String =
            apiKey ?: error("API key not initialized. Did you forget to call initialize()?")
    }
