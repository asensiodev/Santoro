package com.noirsonora.onboarding_domain.di

import com.noirsonora.onboarding_domain.repository.OnboardingRepository
import com.noirsonora.onboarding_domain.use_case.GetOnboardingState
import com.noirsonora.onboarding_domain.use_case.OnboardingUseCases
import com.noirsonora.onboarding_domain.use_case.SaveOnboardingState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object OnboardingDomainModule {

    @ViewModelScoped
    @Provides
    fun provideOnboardingUseCases(
        onboardingRepository: OnboardingRepository
    ): OnboardingUseCases {
        return OnboardingUseCases(
            saveOnboardingState = SaveOnboardingState(onboardingRepository),
            getOnboardingState = GetOnboardingState(onboardingRepository)
        )
    }

}