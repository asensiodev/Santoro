package com.noirsonora.onboarding_data.di

import android.content.Context
import com.noirsonora.onboarding_data.repository.OnboardingDataStoreRepositoryImpl
import com.noirsonora.onboarding_domain.repository.OnboardingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnboardingDataModule {

    @Singleton
    @Provides
    fun provideOnboardingRepository(
        @ApplicationContext context: Context
    ): OnboardingRepository = OnboardingDataStoreRepositoryImpl(context = context)

}