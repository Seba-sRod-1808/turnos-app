package com.example.turnos.ui.screens.forgotpassword

sealed interface ForgotPasswordEvent {
    data class OnUsernameChange(val value: String) : ForgotPasswordEvent
    data class OnAnswerChange(val value: String) : ForgotPasswordEvent
    data class OnNewPasswordChange(val value: String) : ForgotPasswordEvent
    data class OnConfirmPasswordChange(val value: String) : ForgotPasswordEvent
    data object OnTogglePasswordVisibility : ForgotPasswordEvent
    data object OnContinueClick : ForgotPasswordEvent
    data object OnStepBackClick : ForgotPasswordEvent
    data object OnBackToLoginClick : ForgotPasswordEvent
}
