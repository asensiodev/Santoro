package com.noirsonora.login_domain.use_case

import com.noirsonora.core.domain.UserDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOnboardingState @Inject constructor(
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return userDataRepository.readOnboardingState()
    }
}