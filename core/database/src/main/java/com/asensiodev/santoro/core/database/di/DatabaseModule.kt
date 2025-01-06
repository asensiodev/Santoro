package com.asensiodev.santoro.core.database.di

import android.content.Context
import androidx.room.Room
import com.asensiodev.santoro.core.database.data.SantoroRoomDatabase
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.repository.RoomDatabaseRepository
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
    ): SantoroRoomDatabase =
        Room
            .databaseBuilder(
                context,
                SantoroRoomDatabase::class.java,
                DATABASE_NAME,
            ).build()

    @Provides
    fun provideMovieDao(database: SantoroRoomDatabase): MovieDao = database.movieDao()

    @Provides
    @Singleton
    fun provideDatabaseRepository(movieDao: MovieDao): DatabaseRepository =
        RoomDatabaseRepository(movieDao)
}

private const val DATABASE_NAME = "movies_database"
