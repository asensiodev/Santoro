package com.noirsonora.onboarding_presentation.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noirsonora.onboarding_domain.use_case.SaveOnboardingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveOnboardingState: SaveOnboardingState
) : ViewModel() {

    fun saveOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            saveOnboardingState(completed)
        }
    }

}