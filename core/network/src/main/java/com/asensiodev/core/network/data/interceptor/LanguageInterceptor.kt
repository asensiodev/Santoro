package com.asensiodev.core.network.data.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject

class LanguageInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val locale = Locale.getDefault()
            val languageTag =
                "${locale.language}-${locale.country}".takeIf {
                    locale.country.isNotEmpty()
                } ?: locale.language

            val url =
                originalRequest.url
                    .newBuilder()
                    .addQueryParameter(LANGUAGE_PARAM, languageTag)
                    .build()

            val request = originalRequest.newBuilder().url(url).build()
            return chain.proceed(request)
        }

        private companion object {
            const val LANGUAGE_PARAM = "language"
        }
    }
