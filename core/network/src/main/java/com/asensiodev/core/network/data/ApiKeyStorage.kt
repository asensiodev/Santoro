package com.asensiodev.core.network.data

interface ApiKeyStorage {
    fun read(): String?
    fun write(value: String)
    fun clear()
}
