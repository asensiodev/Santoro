package com.asensiodev.feature.moviedetail.impl.di

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.feature.moviedetail.impl.data.datasource.LocalMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.datasource.RetrofitMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.datasource.RoomMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.repository.DefaultMovieDetailRepository
import com.asensiodev.feature.moviedetail.impl.data.service.MovieDetailApiService
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.UpdateMovieStateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object MovieDetailModule {
    @Provides
    fun provideMovieDetailApiService(retrofit: Retrofit): MovieDetailApiService =
        retrofit.create(MovieDetailApiService::class.java)

    @Provides
    @Singleton
    fun provideRemoteMovieDetailDataSource(
        apiService: MovieDetailApiService,
    ): RetrofitMovieDetailDataSource = RetrofitMovieDetailDataSource(apiService)

    @Provides
    @Singleton
    fun provideLocalMovieDetailDataSource(
        roomDataSource: RoomMovieDetailDataSource,
    ): LocalMovieDetailDataSource = roomDataSource

    @Provides
    @Singleton
    fun provideMovieDetailRepository(
        remoteDataSource: RetrofitMovieDetailDataSource,
        localDataSource: RoomMovieDetailDataSource,
    ): MovieDetailRepository = DefaultMovieDetailRepository(localDataSource, remoteDataSource)

    @Provides
    @Singleton
    fun provideGetMovieDetailUseCase(
        repository: MovieDetailRepository,
        dispatchers: DispatcherProvider,
    ): GetMovieDetailUseCase = GetMovieDetailUseCase(repository, dispatchers)

    @Provides
    @Singleton
    fun provideUpdateMovieStateUseCase(
        repository: MovieDetailRepository,
        dispatchers: DispatcherProvider,
    ): UpdateMovieStateUseCase = UpdateMovieStateUseCase(repository, dispatchers)
}
