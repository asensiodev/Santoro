package com.asensiodev.feature.watchlist.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveFromWatchlistUseCase
    @Inject
    constructor(
        private val databaseRepository: DatabaseRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        suspend operator fun invoke(movieId: Int): Result<Boolean> =
            withContext(dispatchers.io) {
                databaseRepository.removeFromWatchlist(movieId)
            }
    }
