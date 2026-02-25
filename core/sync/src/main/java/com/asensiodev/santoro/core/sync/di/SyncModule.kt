package com.asensiodev.santoro.core.sync.di

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkerFactory
import com.asensiodev.santoro.core.sync.data.datasource.FirestoreMovieDataSource
import com.asensiodev.santoro.core.sync.data.datasource.FirestoreMovieDataSourceImpl
import com.asensiodev.santoro.core.sync.data.repository.DefaultSyncRepository
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
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
    abstract fun bindFirestoreMovieDataSource(
        impl: FirestoreMovieDataSourceImpl,
    ): FirestoreMovieDataSource

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: DefaultSyncRepository): SyncRepository

    @Binds
    abstract fun bindWorkerFactory(factory: HiltWorkerFactory): WorkerFactory

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
