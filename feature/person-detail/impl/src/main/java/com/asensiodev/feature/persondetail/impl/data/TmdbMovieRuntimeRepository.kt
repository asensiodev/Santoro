package com.asensiodev.feature.persondetail.impl.data

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.repository.MovieRuntimeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class TmdbMovieRuntimeRepository(
    private val service: MovieRuntimeApiService,
    private val dispatchers: DispatcherProvider,
) : MovieRuntimeRepository {
    private val cache = ConcurrentHashMap<Int, Int>()

    override suspend fun getRuntimes(movieIds: Collection<Int>): Map<Int, Int> {
        val missing = movieIds.distinct().filterNot(cache::containsKey)
        if (missing.isNotEmpty()) fetch(missing)
        return movieIds.mapNotNull { id -> cache[id]?.let { id to it } }.toMap()
    }

    private suspend fun fetch(movieIds: List<Int>) =
        withContext(dispatchers.io) {
            val permits = Semaphore(MAX_PARALLEL_REQUESTS)
            coroutineScope {
                movieIds.map { id -> async { permits.withPermit { cacheRuntime(id) } } }.awaitAll()
            }
        }

    private suspend fun cacheRuntime(id: Int) {
        runtime(id)?.let { cache[id] = it }
    }

    private suspend fun runtime(id: Int): Int? =
        try {
            service.movie(id).runtime?.takeIf { it > 0 }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }

    private companion object {
        const val MAX_PARALLEL_REQUESTS = 6
    }
}
