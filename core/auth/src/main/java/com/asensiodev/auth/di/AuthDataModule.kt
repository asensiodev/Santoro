package com.asensiodev.auth.di

import com.asensiodev.auth.AuthDataSource
import com.asensiodev.auth.FirebaseAuthDataSource
import com.asensiodev.auth.data.repository.DefaultAuthRepository
import com.asensiodev.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AuthDataModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: DefaultAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAuthDataSource(dataSource: FirebaseAuthDataSource): AuthDataSource
}
