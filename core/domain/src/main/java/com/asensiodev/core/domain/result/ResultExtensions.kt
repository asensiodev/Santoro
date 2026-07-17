package com.asensiodev.core.domain.result

import kotlinx.coroutines.CancellationException

fun <T> Result<T>.rethrowCancellation(): Result<T> {
    val exception = exceptionOrNull()
    if (exception is CancellationException) throw exception
    return this
}
