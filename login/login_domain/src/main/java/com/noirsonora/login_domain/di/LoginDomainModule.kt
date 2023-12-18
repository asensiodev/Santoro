package com.noirsonora.login_domain.di

import com.noirsonora.core.domain.UserDataRepository
import com.noirsonora.login_domain.use_case.GetOnboardingState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LoginDomainModule {

    @Provides
    fun providesLoginUseCase(
        userDataRepository: UserDataRepository
    ): GetOnboardingState {
        return GetOnboardingState(userDataRepository)
    }

}