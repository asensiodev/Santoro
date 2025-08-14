package com.asensiodev.library.securestorage.api

interface SecureKeyValueStore {
    fun readString(key: String): String?
    fun writeString(
        key: String,
        value: String,
    )
    fun remove(key: String)
    fun clearAll()
}
