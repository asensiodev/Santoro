package com.asensiodev.feature.watchlist.impl.di

import com.asensiodev.feature.watchlist.impl.domain.usecase.AddMovieToWatchlistUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// TODO(): use viewModelComponent?
@Module
@InstallIn(SingletonComponent::class)
internal object WatchlistMoviesModule {
    @Provides
    @Singleton
    internal fun provideWatchlistMoviesUseCase(
        repository: DatabaseRepository,
    ): GetWatchlistMoviesUseCase = GetWatchlistMoviesUseCase(repository)

    @Provides
    @Singleton
    internal fun provideAddMovieToWatchlistMovieUseCase(
        repository: DatabaseRepository,
    ): AddMovieToWatchlistUseCase = AddMovieToWatchlistUseCase(repository)
}