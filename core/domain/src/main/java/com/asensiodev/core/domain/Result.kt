package com.asensiodev.core.domain

sealed class Result<out T> {
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>()
}

fun <T> Result<T>.getOrDefault(defaultValue: T): T =
    if (this is Result.Success) this.data else defaultValue

fun <T> Result<T>.getOrNull(): T? = if (this is Result.Success) this.data else null
