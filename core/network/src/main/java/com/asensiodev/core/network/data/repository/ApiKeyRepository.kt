package com.asensiodev.core.network.data.repository

import com.asensiodev.core.network.data.ApiKeyStorage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the TMDB API key in memory and in encrypted storage.
 * - getSync(): always returns synchronously (reads memory or storage).
 * - refreshFromRemote(): fetches remote, validates, persists, and updates memory.
 */
@Singleton
class ApiKeyRepository
    @Inject
    constructor(
        private val storage: ApiKeyStorage,
    ) {
        @Volatile
        private var cached: String? = null

        fun getSyncOrNull(): String? {
            val inMemory: String? = cached
            val persisted: String? = if (inMemory == null) storage.read() else null

            return when {
                inMemory != null -> inMemory
                !persisted.isNullOrBlank() -> persisted.also { persisted -> cached = persisted }
                else -> null
            }
        }

        suspend fun refreshFromRemote(fetch: suspend () -> String) {
            val fresh: String = fetch()
            require(fresh.isNotBlank()) { "Fetched API key is blank" }
            storage.write(fresh)
            cached = fresh
        }

        fun clear() {
            cached = null
            storage.clear()
        }
    }
