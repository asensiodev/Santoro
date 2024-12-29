package com.asensiodev.core.network.data.interceptor

interface ApiKeyProvider {
    fun getApiKey(): String?
}
