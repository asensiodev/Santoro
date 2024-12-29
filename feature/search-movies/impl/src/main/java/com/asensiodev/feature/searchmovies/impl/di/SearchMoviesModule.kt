package com.asensiodev.feature.searchmovies.impl.di

import com.asensiodev.feature.searchmovies.impl.data.DefaultSearchMoviesDatasource
import com.asensiodev.feature.searchmovies.impl.data.DefaultSearchMoviesRepository
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
    fun provideMovieDefaultDatasource(
        apiService: SearchMoviesApiService,
    ): DefaultSearchMoviesDatasource = DefaultSearchMoviesDatasource(apiService)

    @Provides
    @Singleton
    fun provideMovieRepository(
        defaultDatasource: DefaultSearchMoviesDatasource,
    ): SearchMoviesRepository = DefaultSearchMoviesRepository(defaultDatasource)

    @Provides
    @Singleton
    internal fun provideSearchMoviesUseCase(
        repository: SearchMoviesRepository,
    ): SearchMoviesUseCase = SearchMoviesUseCase(repository)
}
