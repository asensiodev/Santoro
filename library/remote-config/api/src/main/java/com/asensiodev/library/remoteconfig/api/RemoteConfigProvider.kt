package com.asensiodev.library.remoteconfig.api

interface RemoteConfigProvider {
    fun getStringParameter(remoteConfigName: RemoteConfigName): String
}
