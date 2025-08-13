package com.asensiodev.core.network.di

import com.asensiodev.core.buildconfig.BuildConfig
import com.asensiodev.core.network.data.ApiKeyInitializer
import com.asensiodev.core.network.data.ApiKeyProviderContract
import com.asensiodev.core.network.data.CachedApiKeyProvider
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
    fun provideApiKeyInitializer(remoteConfigProvider: RemoteConfigProvider): ApiKeyInitializer =
        ApiKeyInitializer(remoteConfigProvider)

    @Provides
    @Singleton
    fun provideApiKeyProvider(initializer: ApiKeyInitializer): ApiKeyProviderContract =
        CachedApiKeyProvider(initializer)

    @Provides
    @Singleton
    fun provideOkHttpClient(apiKeyProviderContract: ApiKeyProviderContract): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(AuthorizationInterceptor(apiKeyProviderContract))
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
