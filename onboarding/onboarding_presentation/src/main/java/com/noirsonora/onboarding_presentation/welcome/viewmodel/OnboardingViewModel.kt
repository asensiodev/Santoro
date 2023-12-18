package com.noirsonora.onboarding_presentation.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noirsonora.core.domain.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    fun saveOnboardingState(completed: Boolean) {
        viewModelScope.launch {
            userDataRepository.saveOnboardingState(completed)
        }
    }

}