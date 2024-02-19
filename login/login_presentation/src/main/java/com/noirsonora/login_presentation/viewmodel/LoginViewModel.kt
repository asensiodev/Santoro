package com.noirsonora.login_presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noirsonora.login_domain.use_case.GetOnboardingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getOnboardingState: GetOnboardingState
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
                  }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.LoginClick -> TODO()
            LoginEvent.RegisterClick -> TODO()
        }
    }}