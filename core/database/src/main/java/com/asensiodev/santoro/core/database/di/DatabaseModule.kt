package com.asensiodev.santoro.core.database.di

import android.content.Context
import androidx.room.Room
import com.asensiodev.santoro.core.database.data.DatabaseRepositoryImpl
import com.asensiodev.santoro.core.database.data.RoomDatabaseImpl
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): RoomDatabaseImpl =
        Room
            .databaseBuilder(
                context,
                RoomDatabaseImpl::class.java,
                DATABASE_NAME,
            ).build()

    @Provides
    fun provideMovieDao(database: RoomDatabaseImpl): MovieDao = database.movieDao()

    @Provides
    @Singleton
    fun provideDatabaseRepository(movieDao: MovieDao): DatabaseRepository =
        DatabaseRepositoryImpl(movieDao)
}

private const val DATABASE_NAME = "movies_database"
