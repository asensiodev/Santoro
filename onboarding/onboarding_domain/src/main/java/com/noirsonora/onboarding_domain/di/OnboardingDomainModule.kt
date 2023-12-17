package com.noirsonora.onboarding_domain.di

import com.noirsonora.core.domain.DataStoreRepository
import com.noirsonora.onboarding_domain.use_case.SaveOnboardingState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object OnboardingDomainModule {

    @Provides
    fun provideOnboardingUseCase(
        dataStoreRepository: DataStoreRepository,
        ioDispatcher: CoroutineDispatcher
    ): SaveOnboardingState {
        return SaveOnboardingState(dataStoreRepository, ioDispatcher)
    }

}