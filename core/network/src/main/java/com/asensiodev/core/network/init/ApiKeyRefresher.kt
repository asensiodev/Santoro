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
         * If there is no local key, fetch from Remote Config and persist.
         * If there is already a key, do nothing.
         */
        suspend fun refreshOrNoop() {
            val existing: String? = repository.getSyncOrNull()
            if (!existing.isNullOrBlank()) return

            repository.refreshFromRemote(
                fetch = {
                    remoteConfig.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
                },
            )
        }

        /**
         * If there is a local key, try to fetch from Remote Config and update
         * only when the value is non-blank and different. If RC fails or returns blank, do nothing.
         */
        suspend fun refreshIfChanged() {
            val current: String? = repository.getSyncOrNull()
            if (current.isNullOrBlank()) return

            val remote: String =
                remoteConfig.getStringParameter(
                    RemoteConfigName.TMDB_SANTORO_API_KEY,
                )
            val shouldUpdate: Boolean = remote.isNotBlank() && remote != current
            if (shouldUpdate) {
                repository.refreshFromRemote(fetch = { remote })
            }
        }
    }
