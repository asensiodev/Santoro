package com.asensiodev.feature.watchlist.impl.di

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.feature.watchlist.impl.domain.usecase.AddMovieToWatchlistUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object WatchlistMoviesModule {
    @Provides
    @Singleton
    internal fun provideWatchlistMoviesUseCase(
        repository: DatabaseRepository,
        dispatchers: DispatcherProvider,
    ): GetWatchlistMoviesUseCase = GetWatchlistMoviesUseCase(repository, dispatchers)

    @Provides
    @Singleton
    internal fun provideAddMovieToWatchlistMovieUseCase(
        repository: DatabaseRepository,
        dispatchers: DispatcherProvider,
    ): AddMovieToWatchlistUseCase = AddMovieToWatchlistUseCase(repository, dispatchers)

    @Provides
    @Singleton
    internal fun provideSearchWatchlistMoviesUseCase(
        repository: DatabaseRepository,
        dispatchers: DispatcherProvider,
    ): SearchWatchlistMoviesUseCase = SearchWatchlistMoviesUseCase(repository, dispatchers)
}
