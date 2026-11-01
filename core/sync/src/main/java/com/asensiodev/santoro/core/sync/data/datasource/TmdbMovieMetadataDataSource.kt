package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.santoro.core.data.mapper.toDirectors
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

internal class TmdbMovieMetadataDataSource(
    private val service: MovieMetadataApiService,
) : MovieMetadataRemoteDataSource {
    override suspend fun getMetadata(movieId: Int): Result<RemoteMovieMetadata> =
        try {
            val detail = service.movieDetail(movieId)
            Result.success(
                RemoteMovieMetadata(
                    directors = detail.credits?.toDirectors().orEmpty(),
                    releaseDate = detail.releaseDate.orEmpty(),
                ),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpException) {
            when (exception.code()) {
                HTTP_NOT_FOUND -> Result.success(RemoteMovieMetadata(emptyList(), ""))
                else -> Result.failure(exception)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
