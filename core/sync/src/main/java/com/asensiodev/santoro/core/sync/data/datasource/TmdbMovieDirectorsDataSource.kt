package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.core.domain.model.Director
import com.asensiodev.santoro.core.data.mapper.toDirectors
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

internal class TmdbMovieDirectorsDataSource(
    private val service: MovieCreditsApiService,
) : MovieDirectorsRemoteDataSource {
    override suspend fun getDirectors(movieId: Int): Result<List<Director>> =
        try {
            Result.success(service.credits(movieId).toDirectors())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpException) {
            when (exception.code()) {
                HTTP_NOT_FOUND -> Result.success(emptyList())
                else -> Result.failure(exception)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
