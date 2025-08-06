package com.asensiodev.core.network.init

import com.asensiodev.core.network.data.ApiKeyInitializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApiKeyInitializerEntryPoint {
    fun apiKeyInitializer(): ApiKeyInitializer
}
