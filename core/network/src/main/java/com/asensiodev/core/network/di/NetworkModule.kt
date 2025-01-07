package com.asensiodev.core.network.di

import com.asensiodev.core.buildconfig.BuildConfig
import com.asensiodev.core.network.data.ApiKeyProvider
import com.asensiodev.core.network.data.RemoteConfigApiKeyProvider
import com.asensiodev.core.network.data.interceptor.AuthorizationInterceptor
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
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
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(provideLoggingInterceptor())
                }
            }.build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(TMDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
