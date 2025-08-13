package com.asensiodev.core.network.data.interceptor

import com.asensiodev.core.network.data.ApiKeyProviderContract
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal class AuthorizationInterceptor
    @Inject
    constructor(
        private val apiKeyProviderContract: ApiKeyProviderContract,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val apiKey = apiKeyProviderContract.getApiKey()
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
