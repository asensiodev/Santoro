package com.asensiodev.core.network.data

import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import javax.inject.Inject

internal class RemoteConfigApiKeyProvider
    @Inject
    constructor(
        private val remoteConfigProvider: RemoteConfigProvider,
    ) : ApiKeyProvider {
        private val cachedApiKey: String by lazy {
            remoteConfigProvider
                .getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
                .also { check(it.isNotBlank()) { "API key is missing!" } }
        }

        override fun getApiKey(): String = cachedApiKey
    }
