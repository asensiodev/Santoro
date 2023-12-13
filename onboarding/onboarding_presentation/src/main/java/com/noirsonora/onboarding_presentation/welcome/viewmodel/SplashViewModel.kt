package com.noirsonora.onboarding_presentation.welcome.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noirsonora.core.navigation.Route
import com.noirsonora.onboarding_domain.use_case.OnboardingUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingUseCases: OnboardingUseCases
) : ViewModel() {

    private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _startDestination: MutableState<String> = mutableStateOf(Route.ONBOARDING)
    val startDestination: State<String> = _startDestination

    init {
        viewModelScope.launch {
            onboardingUseCases.getOnboardingState().collect { completed ->
                if (completed) {
                    _startDestination.value = Route.LOGIN
                } else {
                    _startDestination.value = Route.ONBOARDING
                }
                _isLoading.value = false
            }
        }
    }

}