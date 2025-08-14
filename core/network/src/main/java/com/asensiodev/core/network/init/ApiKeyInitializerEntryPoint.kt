package com.asensiodev.core.network.init

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApiKeyInitializerEntryPoint {
    fun apiKeyRefresher(): ApiKeyRefresher
}
