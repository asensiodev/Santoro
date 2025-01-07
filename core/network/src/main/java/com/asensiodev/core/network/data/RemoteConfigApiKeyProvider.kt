package com.asensiodev.core.network.data

import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import javax.inject.Inject

internal class RemoteConfigApiKeyProvider
    @Inject
    constructor(
        private val remoteConfigProvider: RemoteConfigProvider,
    ) : ApiKeyProvider {
        override fun getApiKey(): String =
            remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
    }
