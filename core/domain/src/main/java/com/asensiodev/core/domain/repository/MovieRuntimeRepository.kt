package com.asensiodev.core.domain.repository

interface MovieRuntimeRepository {
    suspend fun getRuntimes(movieIds: Collection<Int>): Map<Int, Int>
}
