package com.asensiodev.santoro.core.sync.di

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkerFactory
import com.asensiodev.core.domain.repository.SyncRepository
import com.asensiodev.core.domain.repository.SyncScheduler
import com.asensiodev.santoro.core.sync.data.datasource.FirestoreMovieDataSource
import com.asensiodev.santoro.core.sync.data.datasource.MovieSyncRemoteDataSource
import com.asensiodev.santoro.core.sync.data.repository.DefaultSyncRepository
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SyncModule {
    @Binds
    @Singleton
    abstract fun bindMovieSyncRemoteDataSource(
        dataSource: FirestoreMovieDataSource,
    ): MovieSyncRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: DefaultSyncRepository): SyncRepository

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler

    @Binds
    abstract fun bindWorkerFactory(factory: HiltWorkerFactory): WorkerFactory

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
