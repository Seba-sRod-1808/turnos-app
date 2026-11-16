package com.example.turnos.ui.screens.setupaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.navigation.SetupAccountDestination
import com.example.turnos.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Primer ingreso con la contraseña temporal que entregó el local: el cliente elige su propia
 * contraseña y una pregunta de seguridad para poder recuperarla después sin ayuda.
 */

class SetupAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val userId = savedStateHandle.toRoute<SetupAccountDestination>().userId

    private val _state = MutableStateFlow(SetupAccountState())
    val state: StateFlow<SetupAccountState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = authRepository.getUserName(userId).orEmpty()
            _state.update { it.copy(userName = name.substringBefore(" ")) }
        }
    }

    fun onEvent(event: SetupAccountEvent) {
        when (event) {
            is SetupAccountEvent.OnNewPasswordChange -> _state.update { it.copy(newPassword = event.value, passwordError = null) }
            is SetupAccountEvent.OnConfirmPasswordChange -> _state.update { it.copy(confirmPassword = event.value, confirmError = null) }
            SetupAccountEvent.OnTogglePasswordVisibility -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            is SetupAccountEvent.OnQuestionSelected -> _state.update { it.copy(selectedQuestion = event.question) }
            is SetupAccountEvent.OnAnswerChange -> _state.update { it.copy(answer = event.value, answerError = null) }
            SetupAccountEvent.OnSaveClick -> save()
            SetupAccountEvent.OnNavigationHandled -> _state.update { it.copy(completedRole = null) }
            SetupAccountEvent.OnCancelClick -> Unit
        }
    }

    private fun save() {
        val s = _state.value
        val validated = s.copy(
            passwordError = if (!Validators.isValidPassword(s.newPassword)) "Mínimo ${Validators.MIN_PASSWORD_LENGTH} caracteres" else null,
            confirmError = if (s.confirmPassword != s.newPassword) "Las contraseñas no coinciden" else null,
            answerError = if (!Validators.isValidSecurityAnswer(s.answer)) "Escribe una respuesta que recuerdes" else null,
        )
        _state.value = validated
        if (listOf(validated.passwordError, validated.confirmError, validated.answerError).any { it != null }) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            authRepository.completeSetup(userId, s.newPassword, s.selectedQuestion, s.answer)
                .onSuccess { user -> _state.update { it.copy(isSaving = false, completedRole = user.role) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, errorMessage = e.message) } }
        }
    }
}
