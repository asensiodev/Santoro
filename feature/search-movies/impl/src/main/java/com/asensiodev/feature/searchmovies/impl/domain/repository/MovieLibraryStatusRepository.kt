package com.asensiodev.feature.searchmovies.impl.domain.repository

import kotlinx.coroutines.flow.Flow

internal interface MovieLibraryStatusRepository {
    fun observeWatchedMovieIds(): Flow<Result<Set<Int>>>

    fun observeWatchlistMovieIds(): Flow<Result<Set<Int>>>
}
