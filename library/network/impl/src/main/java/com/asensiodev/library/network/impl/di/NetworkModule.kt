package com.asensiodev.library.network.impl.di

import com.asensiodev.library.network.api.MovieRepository
import com.asensiodev.library.network.impl.data.DefaultMovieDatasource
import com.asensiodev.library.network.impl.data.DefaultMovieRepository
import com.asensiodev.library.network.impl.data.MovieApiService
import com.asensiodev.library.network.impl.data.interceptor.ApiKeyProvider
import com.asensiodev.library.network.impl.data.interceptor.AuthorizationInterceptor
import com.asensiodev.library.network.impl.data.interceptor.RemoteConfigApiKeyProvider
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun provideApiKeyProvider(remoteConfigProvider: RemoteConfigProvider): ApiKeyProvider =
        RemoteConfigApiKeyProvider(remoteConfigProvider)

    @Provides
    @Singleton
    fun provideOkHttpClient(apiKeyProvider: ApiKeyProvider): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(AuthorizationInterceptor(apiKeyProvider))
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideMovieApiService(retrofit: Retrofit): MovieApiService =
        retrofit.create(MovieApiService::class.java)

    @Provides
    @Singleton
    fun provideMovieDefaultDatasource(apiService: MovieApiService): DefaultMovieDatasource =
        DefaultMovieDatasource(apiService)

    @Provides
    @Singleton
    fun provideMovieRepository(defaultDatasource: DefaultMovieDatasource): MovieRepository =
        DefaultMovieRepository(defaultDatasource)
}
