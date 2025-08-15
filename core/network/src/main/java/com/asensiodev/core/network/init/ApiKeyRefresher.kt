package com.asensiodev.core.network.init

import com.asensiodev.core.network.data.repository.ApiKeyRepository
import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyRefresher
    @Inject
    constructor(
        private val repository: ApiKeyRepository,
        private val remoteConfig: RemoteConfigProvider,
    ) {
        /**
         * Ensures the local API key is present and up-to-date:
         * - If there is no local key: tries to fetch from Remote Config and persist.
         * - If there is a local key: fetches from RC and updates only when non-blank and different.
         * If RC fails or returns blank, this method is a no-op.
         */
        suspend fun ensureKeyUpToDate() {
            val current: String? = repository.getSyncOrNull()

            val remote: String =
                remoteConfig.getStringParameter(
                    RemoteConfigName.TMDB_SANTORO_API_KEY,
                )
            if (remote.isBlank()) {
                return
            }

            val shouldUpdate: Boolean = current.isNullOrBlank() || remote != current
            if (shouldUpdate) {
                repository.refreshFromRemote(fetch = { remote })
            }
        }
    }
