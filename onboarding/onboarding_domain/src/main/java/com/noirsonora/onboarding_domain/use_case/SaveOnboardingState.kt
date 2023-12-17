package com.noirsonora.onboarding_domain.use_case

import com.noirsonora.core.dagger_coroutines.IoDispatcher
import com.noirsonora.core.domain.DataStoreRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveOnboardingState @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(completed: Boolean) = withContext(ioDispatcher) {
        dataStoreRepository.saveOnboardingState(completed)
    }
}

