package com.asensiodev.feature.watchedmovies.impl.di

import com.asensiodev.feature.watchedmovies.impl.domain.usecase.AddWatchedMovieUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object WatchedMoviesModule {
    @Provides
    @Singleton
    internal fun provideWatchedMoviesUseCase(
        repository: DatabaseRepository,
    ): GetWatchedMoviesUseCase = GetWatchedMoviesUseCase(repository)

    @Provides
    @Singleton
    internal fun provideAddWatchedMovieUseCase(
        repository: DatabaseRepository,
    ): AddWatchedMovieUseCase = AddWatchedMovieUseCase(repository)

    @Provides
    @Singleton
    internal fun provideSearchWatchedMoviesUseCase(
        repository: DatabaseRepository,
    ): SearchWatchedMoviesUseCase = SearchWatchedMoviesUseCase(repository)
}
