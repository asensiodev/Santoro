package com.asensiodev.securestorage.impl.di

import android.content.Context
import com.asensiodev.library.securestorage.api.SecureKeyValueStore
import com.asensiodev.securestorage.impl.EncryptedPrefsSecureKeyValueStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecureStorageModule {
    @Provides
    @Singleton
    @Named("api_key_store")
    fun provideApiKeySecureStore(
        @ApplicationContext context: Context,
    ): SecureKeyValueStore =
        EncryptedPrefsSecureKeyValueStore(
            context = context,
            fileName = FILE_API,
        )

    @Provides
    @Singleton
    @Named("user_preferences")
    fun provideUserPreferencesSecureStore(
        @ApplicationContext context: Context,
    ): SecureKeyValueStore =
        EncryptedPrefsSecureKeyValueStore(
            context = context,
            fileName = FILE_USER_PREFS,
        )
}

private const val FILE_API = "secure_api_store"
private const val FILE_USER_PREFS = "secure_user_prefs"
