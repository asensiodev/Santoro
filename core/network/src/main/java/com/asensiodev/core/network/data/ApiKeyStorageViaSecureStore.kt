package com.asensiodev.core.network.data

import com.asensiodev.library.securestorage.api.SecureKeyValueStore
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ApiKeyStorageViaSecureStore
    @Inject
    constructor(
        @Named("api_key_store") private val secureStore: SecureKeyValueStore,
    ) : ApiKeyStorage {
        override fun read(): String? = secureStore.readString(KEY_NAME)

        override fun write(value: String) {
            secureStore.writeString(KEY_NAME, value)
        }

        override fun clear() {
            secureStore.remove(KEY_NAME)
        }
    }

private const val KEY_NAME = "tmdb_api_key"
