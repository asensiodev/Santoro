package com.asensiodev.core.network.api

interface ApiKeyProviderContract {
    fun getApiKey(): String?
}
