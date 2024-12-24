package com.asensiodev.library.remoteconfig.impl.di

import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import com.asensiodev.library.remoteconfig.impl.FirebaseRemoteConfigProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RemoteConfigModule {
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = Firebase.remoteConfig

    @Provides
    @Singleton
    fun provideRemoteConfigProvider(
        firebaseRemoteConfig: FirebaseRemoteConfig,
    ): RemoteConfigProvider = FirebaseRemoteConfigProvider(firebaseRemoteConfig)
}
