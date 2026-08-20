@file:Suppress(
    "EXPOSED_FUNCTION_RETURN_TYPE",
    "EXPOSED_PARAMETER_TYPE",
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
)

package com.asensiodev.santoro.di

import com.asensiodev.auth.di.AuthDataModule
import com.asensiodev.auth.di.FirebaseModule
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.moviedetail.impl.di.MovieDetailModule
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import com.asensiodev.feature.searchmovies.impl.data.repository.DefaultMovieLibraryStatusRepository
import com.asensiodev.feature.searchmovies.impl.di.SearchMoviesModule
import com.asensiodev.feature.searchmovies.impl.domain.repository.MovieLibraryStatusRepository
import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.asensiodev.library.observability.impl.FirebaseObservabilityModule
import com.asensiodev.library.observability.impl.ObservabilityModule
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import com.asensiodev.library.remoteconfig.impl.di.RemoteConfigModule
import com.asensiodev.santoro.core.data.di.RepositoryModule
import com.asensiodev.santoro.core.database.di.DatabaseModule
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.di.SyncModule
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import com.asensiodev.santoro.fake.FakeAuthRepository
import com.asensiodev.santoro.fake.FakeDatabaseRepository
import com.asensiodev.santoro.fake.FakeMovieDetailRepository
import com.asensiodev.santoro.fake.FakeRecentSearchesRepository
import com.asensiodev.santoro.fake.FakeRemoteConfigProvider
import com.asensiodev.santoro.fake.FakeSearchMoviesRepository
import com.asensiodev.santoro.fake.FakeSyncRepository
import com.asensiodev.santoro.fake.FakeUserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        AuthDataModule::class,
        FirebaseModule::class,
        SearchMoviesModule::class,
        MovieDetailModule::class,
        DatabaseModule::class,
        RepositoryModule::class,
        SyncModule::class,
        ObservabilityModule::class,
        FirebaseObservabilityModule::class,
        RemoteConfigModule::class,
    ],
)
object AppJourneyTestModule {
    @Provides
    @Singleton
    fun provideFakeAuthRepository() = FakeAuthRepository()

    @Provides
    @Singleton
    fun provideAuthRepository(fake: FakeAuthRepository): AuthRepository = fake

    @Provides
    @Singleton
    fun provideFakeUserPreferencesRepository() = FakeUserPreferencesRepository()

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        fake: FakeUserPreferencesRepository,
    ): UserPreferencesRepository = fake

    @Provides
    @Singleton
    fun provideFakeDatabaseRepository() = FakeDatabaseRepository()

    @Provides
    @Singleton
    fun provideDatabaseRepository(fake: FakeDatabaseRepository): DatabaseRepository = fake

    @Provides
    @Singleton
    fun provideFakeSearchMoviesRepository() = FakeSearchMoviesRepository()

    @Provides
    @Singleton
    fun provideSearchMoviesRepository(fake: FakeSearchMoviesRepository): SearchMoviesRepository =
        fake

    @Provides
    fun provideMovieLibraryStatusRepository(
        databaseRepository: DatabaseRepository,
    ): MovieLibraryStatusRepository = DefaultMovieLibraryStatusRepository(databaseRepository)

    @Provides
    @Singleton
    fun provideFakeRecentSearchesRepository() = FakeRecentSearchesRepository()

    @Provides
    @Singleton
    fun provideRecentSearchesRepository(
        fake: FakeRecentSearchesRepository,
    ): RecentSearchesRepository = fake

    @Provides
    @Singleton
    fun provideFakeMovieDetailRepository() = FakeMovieDetailRepository()

    @Provides
    @Singleton
    fun provideMovieDetailRepository(fake: FakeMovieDetailRepository): MovieDetailRepository = fake

    @Provides
    @Singleton
    fun provideFakeSyncRepository() = FakeSyncRepository()

    @Provides
    @Singleton
    fun provideSyncRepository(fake: FakeSyncRepository): SyncRepository = fake

    @Provides
    @Singleton
    fun provideFakeRemoteConfigProvider() = FakeRemoteConfigProvider()

    @Provides
    @Singleton
    fun provideRemoteConfigProvider(fake: FakeRemoteConfigProvider): RemoteConfigProvider = fake

    @Provides
    @Singleton
    fun provideObservabilityTracker(): ObservabilityTracker = NoOpObservabilityTracker
}
