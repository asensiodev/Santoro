package com.asensiodev.core.network.di

import com.asensiodev.core.buildconfig.BuildConfig
import com.asensiodev.core.network.api.ApiKeyProviderContract
import com.asensiodev.core.network.data.ApiKeyStorage
import com.asensiodev.core.network.data.ApiKeyStorageViaSecureStore
import com.asensiodev.core.network.data.CachedApiKeyProvider
import com.asensiodev.core.network.data.auth.ApiKeyAuthenticator
import com.asensiodev.core.network.data.interceptor.AuthorizationInterceptor
import com.asensiodev.core.network.data.repository.ApiKeyRepository
import dagger.Binds
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
abstract class NetworkBindsModule {
    @Binds
    @Singleton
    abstract fun bindApiKeyStorage(impl: ApiKeyStorageViaSecureStore): ApiKeyStorage

    @Binds
    @Singleton
    abstract fun bindApiKeyProvider(impl: CachedApiKeyProvider): ApiKeyProviderContract
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkProvidesModule {
    @Provides
    @Singleton
    fun provideApiKeyRepository(storage: ApiKeyStorage): ApiKeyRepository =
        ApiKeyRepository(storage)

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyProvider: ApiKeyProviderContract,
        logging: HttpLoggingInterceptor,
        authenticator: ApiKeyAuthenticator,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(AuthorizationInterceptor(apiKeyProvider))
        builder.authenticator(authenticator)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(logging)
        }
        return builder.build()
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
