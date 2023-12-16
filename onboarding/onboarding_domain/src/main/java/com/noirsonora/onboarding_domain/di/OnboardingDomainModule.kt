package com.noirsonora.onboarding_domain.di

import com.noirsonora.core.domain.DataStoreRepository
import com.noirsonora.onboarding_domain.use_case.SaveOnboardingState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object OnboardingDomainModule {

    @Provides
    fun provideOnboardingUseCase(
        dataStoreRepository: DataStoreRepository
    ): SaveOnboardingState {
        return SaveOnboardingState(dataStoreRepository)
    }

}