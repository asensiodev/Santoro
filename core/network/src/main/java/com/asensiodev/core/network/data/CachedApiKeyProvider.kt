package com.asensiodev.core.network.data

import jakarta.inject.Inject

internal class CachedApiKeyProvider
    @Inject
    constructor(
        private val initializer: ApiKeyInitializer,
    ) : ApiKeyProviderContract {
        override fun getApiKey(): String = initializer.getCachedApiKey()
    }
