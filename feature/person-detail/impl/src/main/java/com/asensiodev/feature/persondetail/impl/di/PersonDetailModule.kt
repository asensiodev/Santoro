package com.asensiodev.feature.persondetail.impl.di

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.repository.MovieRuntimeRepository
import com.asensiodev.core.domain.repository.PersonRepository
import com.asensiodev.feature.persondetail.impl.data.DefaultPersonRepository
import com.asensiodev.feature.persondetail.impl.data.MovieRuntimeApiService
import com.asensiodev.feature.persondetail.impl.data.PersonApiService
import com.asensiodev.feature.persondetail.impl.data.TmdbMovieRuntimeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PersonDetailModule {
    @Provides
    @Singleton
    fun providePersonRepository(
        retrofit: Retrofit,
        dispatchers: DispatcherProvider,
    ): PersonRepository =
        DefaultPersonRepository(retrofit.create(PersonApiService::class.java), dispatchers)

    @Provides
    @Singleton
    fun provideMovieRuntimeRepository(
        retrofit: Retrofit,
        dispatchers: DispatcherProvider,
    ): MovieRuntimeRepository =
        TmdbMovieRuntimeRepository(retrofit.create(MovieRuntimeApiService::class.java), dispatchers)
}
