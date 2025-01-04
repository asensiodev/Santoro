package com.asensiodev.feature.searchmovies.impl.di

import com.asensiodev.feature.searchmovies.impl.data.RemoteSearchMoviesDatasource
import com.asensiodev.feature.searchmovies.impl.data.RemoteSearchMoviesRepository
import com.asensiodev.feature.searchmovies.impl.data.service.SearchMoviesApiService
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// TODO(): use viewModelComponent?
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
    ): SearchMoviesUseCase = SearchMoviesUseCase(repository)
}
