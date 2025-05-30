package com.asensiodev.core.network.data.interceptor

import com.asensiodev.core.network.data.ApiKeyProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal class AuthorizationInterceptor
    @Inject
    constructor(
        private val apiKeyProvider: ApiKeyProvider,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            // TODO: remove this call from interceptor
            val apiKey = apiKeyProvider.getApiKey()
            check(apiKey != null) { "API key is missing!" }
            val originalRequest = chain.request()
            val requestWithHeaders =
                originalRequest
                    .newBuilder()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
            return chain.proceed(requestWithHeaders)
        }
    }
