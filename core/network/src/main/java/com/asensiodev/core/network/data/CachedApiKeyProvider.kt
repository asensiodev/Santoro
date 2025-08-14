package com.asensiodev.core.network.data

import com.asensiodev.core.network.api.ApiKeyProviderContract
import com.asensiodev.core.network.data.repository.ApiKeyRepository
import javax.inject.Inject

class CachedApiKeyProvider
    @Inject
    constructor(
        private val repository: ApiKeyRepository,
    ) : ApiKeyProviderContract {
        override fun getApiKey(): String? = repository.getSyncOrNull()
    }
