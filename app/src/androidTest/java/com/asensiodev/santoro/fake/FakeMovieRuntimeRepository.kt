package com.asensiodev.santoro.fake

import com.asensiodev.core.domain.repository.MovieRuntimeRepository

class FakeMovieRuntimeRepository : MovieRuntimeRepository {
    override suspend fun getRuntimes(movieIds: Collection<Int>): Map<Int, Int> = emptyMap()
}
