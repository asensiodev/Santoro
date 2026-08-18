package com.asensiodev.feature.searchmovies.impl.data.repository

import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.feature.searchmovies.impl.domain.repository.MovieLibraryStatusRepository
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultMovieLibraryStatusRepository
    @Inject
    constructor(
        private val databaseRepository: DatabaseRepository,
    ) : MovieLibraryStatusRepository {
        override fun observeWatchedMovieIds() =
            databaseRepository.getWatchedMovies().map { result ->
                result.rethrowCancellation().map { movies ->
                    movies.map { movie -> movie.id }.toSet()
                }
            }

        override fun observeWatchlistMovieIds() =
            databaseRepository.getWatchlistMovies().map { result ->
                result.rethrowCancellation().map { movies ->
                    movies.map { movie -> movie.id }.toSet()
                }
            }
    }
