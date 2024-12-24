package com.asensiodev.library.remoteconfig.impl

import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigProvider
    @Inject
    constructor(
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
    ) : RemoteConfigProvider {
        init {
            val remoteConfigSettings =
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = MINIMUM_FETCH_INTERVAL_IN_SECONDS
                }
            firebaseRemoteConfig.setConfigSettingsAsync(remoteConfigSettings)
            firebaseRemoteConfig.fetchAndActivate()
        }

        override fun getStringParameter(remoteConfigName: RemoteConfigName): String =
            firebaseRemoteConfig.getString(remoteConfigName.name)
    }

const val MINIMUM_FETCH_INTERVAL_IN_SECONDS = 3_600L
