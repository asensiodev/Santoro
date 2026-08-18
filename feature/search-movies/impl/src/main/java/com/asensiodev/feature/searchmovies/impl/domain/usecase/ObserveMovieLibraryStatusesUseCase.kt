package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus
import com.asensiodev.feature.searchmovies.impl.domain.repository.MovieLibraryStatusRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class ObserveMovieLibraryStatusesUseCase
    @Inject
    constructor(
        private val repository: MovieLibraryStatusRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke() =
            combine(
                repository.observeWatchedMovieIds(),
                repository.observeWatchlistMovieIds(),
            ) { watchedResult, watchlistResult ->
                val watched = watchedResult.rethrowCancellation()
                val watchlist = watchlistResult.rethrowCancellation()

                watched.fold(
                    onSuccess = { watchedIds ->
                        watchlist.map { watchlistIds ->
                            watchlistIds.associateWith { MovieLibraryStatus.Watchlist } +
                                watchedIds.associateWith { MovieLibraryStatus.Watched }
                        }
                    },
                    onFailure = { exception -> Result.failure(exception) },
                )
            }.flowOn(dispatchers.io)
    }
