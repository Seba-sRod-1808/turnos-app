package com.example.turnos.ui.screens.setupaccount

sealed interface SetupAccountEvent {
    data class OnNewPasswordChange(val value: String) : SetupAccountEvent
    data class OnConfirmPasswordChange(val value: String) : SetupAccountEvent
    data object OnTogglePasswordVisibility : SetupAccountEvent
    data class OnQuestionSelected(val question: String) : SetupAccountEvent
    data class OnAnswerChange(val value: String) : SetupAccountEvent
    data object OnSaveClick : SetupAccountEvent
    data object OnNavigationHandled : SetupAccountEvent
    data object OnCancelClick : SetupAccountEvent
}
