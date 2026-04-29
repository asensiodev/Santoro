package com.asensiodev.library.remoteconfig.api

interface RemoteConfigProvider {
    suspend fun initialize()

    fun getStringParameter(remoteConfigName: RemoteConfigName): String
}
