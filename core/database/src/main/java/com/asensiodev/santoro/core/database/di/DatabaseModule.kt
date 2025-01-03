package com.asensiodev.santoro.core.database.di

import android.content.Context
import androidx.room.Room
import com.asensiodev.santoro.core.database.data.MovieDao
import com.asensiodev.santoro.core.database.data.RoomDatabaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(context: Context): RoomDatabaseImpl =
        Room
            .databaseBuilder(
                context,
                RoomDatabaseImpl::class.java,
                DATABASE_NAME,
            ).build()

    @Provides
    fun provideMovieDao(database: RoomDatabaseImpl): MovieDao = database.movieDao()
}

private const val DATABASE_NAME = "movies_database"
