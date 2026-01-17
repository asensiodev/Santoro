package com.asensiodev.securestorage.impl

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.asensiodev.library.securestorage.api.SecureKeyValueStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPrefsSecureKeyValueStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val fileName: String,
    ) : SecureKeyValueStore {
        private val masterKey: MasterKey =
            MasterKey
                .Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        private val prefs = createEncryptedPrefs()

        private fun createEncryptedPrefs(): android.content.SharedPreferences =
            try {
                buildEncryptedPrefs()
            } catch (_: Exception) {
                // If the key is invalid (e.g. after a reinstall or restore), delete the old file and try again.
                // This prevents the app from crashing on startup.
                context.deleteSharedPreferences(fileName)
                buildEncryptedPrefs()
            }

        private fun buildEncryptedPrefs(): android.content.SharedPreferences =
            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

        override fun readString(key: String): String? = prefs.getString(key, null)

        override fun writeString(
            key: String,
            value: String,
        ) {
            prefs.edit { putString(key, value) }
        }

        override fun remove(key: String) {
            prefs.edit { remove(key) }
        }

        override fun clearAll() {
            prefs.edit { clear() }
        }
    }
