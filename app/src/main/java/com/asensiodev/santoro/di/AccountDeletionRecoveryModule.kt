package com.asensiodev.santoro.di

import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.santoro.data.repository.SharedPreferencesAccountDeletionRecoveryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface AccountDeletionRecoveryModule {
    @Binds
    fun bindAccountDeletionRecoveryRepository(
        repository: SharedPreferencesAccountDeletionRecoveryRepository,
    ): AccountDeletionRecoveryRepository
}
