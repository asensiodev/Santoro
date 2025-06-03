package com.asensiodev.feature.searchmovies.impl.di

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.feature.searchmovies.impl.data.datasource.RemoteSearchMoviesDatasource
import com.asensiodev.feature.searchmovies.impl.data.repository.RemoteSearchMoviesRepository
import com.asensiodev.feature.searchmovies.impl.data.service.SearchMoviesApiService
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SearchMoviesModule {
    @Provides
    fun provideSearchMoviesApiService(retrofit: Retrofit): SearchMoviesApiService =
        retrofit.create(SearchMoviesApiService::class.java)

    @Provides
    @Singleton
    fun provideRemoteSearchMoviesDatasource(
        apiService: SearchMoviesApiService,
    ): RemoteSearchMoviesDatasource = RemoteSearchMoviesDatasource(apiService)

    @Provides
    @Singleton
    fun provideMovieRepository(
        defaultDatasource: RemoteSearchMoviesDatasource,
    ): SearchMoviesRepository = RemoteSearchMoviesRepository(defaultDatasource)

    @Provides
    @Singleton
    internal fun provideSearchMoviesUseCase(
        repository: SearchMoviesRepository,
        dispatchers: DispatcherProvider,
    ): SearchMoviesUseCase = SearchMoviesUseCase(repository, dispatchers)

    @Provides
    @Singleton
    internal fun provideGetPopularMoviesUseCase(
        repository: SearchMoviesRepository,
        dispatchers: DispatcherProvider,
    ): GetPopularMoviesUseCase = GetPopularMoviesUseCase(repository, dispatchers)
}
