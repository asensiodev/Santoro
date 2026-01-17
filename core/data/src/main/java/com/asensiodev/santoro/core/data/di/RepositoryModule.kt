package com.asensiodev.santoro.core.data.di

import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.santoro.core.data.repository.DefaultUserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindUserPreferencesRepository(
        userPreferencesRepository: DefaultUserPreferencesRepository,
    ): UserPreferencesRepository
}
