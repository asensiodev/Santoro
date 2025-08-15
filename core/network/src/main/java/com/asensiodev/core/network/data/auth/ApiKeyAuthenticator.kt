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
            var newRequest: Request? = null

            val attempts: Int = countAuthenticationAttempts(response)
            if (attempts < 1) {
                val wasKeyRefreshed: Boolean =
                    runBlocking {
                        try {
                            refresher.ensureKeyUpToDate()
                            val refreshedApiKey: String? = repository.getSyncOrNull()
                            !refreshedApiKey.isNullOrBlank()
                        } catch (_: Throwable) {
                            false
                        }
                    }

                if (wasKeyRefreshed) {
                    val currentApiKey: String? = repository.getSyncOrNull()
                    if (!currentApiKey.isNullOrBlank()) {
                        newRequest =
                            response.request
                                .newBuilder()
                                .header("Authorization", "Bearer $currentApiKey")
                                .build()
                    }
                }
            }

            return newRequest
        }

        private fun countAuthenticationAttempts(response: Response): Int {
            var count = 1
            var previousResponse: Response? = response.priorResponse
            while (previousResponse != null) {
                count += 1
                previousResponse = previousResponse.priorResponse
            }
            return count
        }
    }
