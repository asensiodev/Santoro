package com.asensiodev.feature.watchedmovies.impl.di

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
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
        dispatchers: DispatcherProvider,
    ): GetWatchedMoviesUseCase = GetWatchedMoviesUseCase(repository, dispatchers)

    @Provides
    @Singleton
    internal fun provideAddWatchedMovieUseCase(
        repository: DatabaseRepository,
        dispatchers: DispatcherProvider,
    ): AddWatchedMovieUseCase = AddWatchedMovieUseCase(repository, dispatchers)

    @Provides
    @Singleton
    internal fun provideSearchWatchedMoviesUseCase(
        repository: DatabaseRepository,
        dispatchers: DispatcherProvider,
    ): SearchWatchedMoviesUseCase = SearchWatchedMoviesUseCase(repository, dispatchers)
}
