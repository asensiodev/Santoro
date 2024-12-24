package com.asensiodev.library.network.impl.data.interceptor

import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import javax.inject.Inject

class RemoteConfigApiKeyProvider
    @Inject
    constructor(
        private val remoteConfigProvider: RemoteConfigProvider,
    ) : ApiKeyProvider {
        override fun getApiKey(): String =
            remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
    }
