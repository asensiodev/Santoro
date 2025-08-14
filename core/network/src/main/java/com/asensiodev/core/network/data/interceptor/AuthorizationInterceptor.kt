package com.asensiodev.core.network.data.interceptor

import com.asensiodev.core.network.api.ApiKeyProviderContract
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthorizationInterceptor
    @Inject
    constructor(
        private val apiKeyProvider: ApiKeyProviderContract,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val apiKey: String? = apiKeyProvider.getApiKey()

            val originalRequest = chain.request()
            val builder = originalRequest.newBuilder()
            builder.addHeader("Accept", "application/json")

            if (!apiKey.isNullOrBlank()) {
                builder.addHeader("Authorization", "Bearer $apiKey")
            }

            return chain.proceed(builder.build())
        }
    }
