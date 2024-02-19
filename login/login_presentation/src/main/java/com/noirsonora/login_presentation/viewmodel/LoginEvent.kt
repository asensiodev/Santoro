package com.noirsonora.login_presentation.viewmodel

sealed class LoginEvent {
    object LoginClick: LoginEvent()
    object RegisterClick: LoginEvent()
}