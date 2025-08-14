package com.asensiodev.core.network.data.auth

import com.asensiodev.core.network.data.repository.ApiKeyRepository
import com.asensiodev.core.network.init.ApiKeyRefresher
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyAuthenticator
    @Inject
    constructor(
        private val refresher: ApiKeyRefresher,
        private val repository: ApiKeyRepository,
    ) : Authenticator {
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            val attempts: Int = countAttempts(response)
            val shouldRetry: Boolean = attempts < 1

            val refreshed: Boolean =
                if (shouldRetry) {
                    runBlocking {
                        val before: String? = repository.getSyncOrNull()
                        try {
                            refresher.refreshOrNoop()
                            refresher.refreshIfChanged()
                        } catch (_: Throwable) {
                            false
                        }?.let {
                            val after: String? = repository.getSyncOrNull()
                            !after.isNullOrBlank() && after != before
                        } ?: false
                    }
                } else {
                    false
                }

            return if (refreshed) {
                val newApiKey: String = repository.getSync()
                response.request
                    .newBuilder()
                    .header("Authorization", "Bearer $newApiKey")
                    .build()
            } else {
                null
            }
        }

        private fun countAttempts(response: Response): Int {
            var count = 1
            var prior: Response? = response.priorResponse
            while (prior != null) {
                count += 1
                prior = prior.priorResponse
            }
            return count
        }
    }
