package com.asensiodev.core.network.data

interface ApiKeyProvider {
    fun getApiKey(): String?
}
