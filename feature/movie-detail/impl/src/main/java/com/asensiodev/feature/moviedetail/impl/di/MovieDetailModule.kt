package com.asensiodev.feature.moviedetail.impl.di

import com.asensiodev.feature.moviedetail.impl.data.DefaultMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.DefaultMovieDetailRepository
import com.asensiodev.feature.moviedetail.impl.data.service.MovieDetailApiService
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// TODO(): use viewModelComponent?
@Module
@InstallIn(SingletonComponent::class)
internal object MovieDetailModule {
    @Provides
    fun provideSearchMoviesApiService(retrofit: Retrofit): MovieDetailApiService =
        retrofit.create(MovieDetailApiService::class.java)

    @Provides
    @Singleton
    fun provideMovieDefaultDatasource(
        apiService: MovieDetailApiService,
    ): DefaultMovieDetailDataSource = DefaultMovieDetailDataSource(apiService)

    @Provides
    @Singleton
    fun provideMovieRepository(
        defaultDatasource: DefaultMovieDetailDataSource,
    ): MovieDetailRepository = DefaultMovieDetailRepository(defaultDatasource)

    @Provides
    @Singleton
    internal fun provideSearchMoviesUseCase(
        repository: MovieDetailRepository,
    ): GetMovieDetailUseCase = GetMovieDetailUseCase(repository)
}
