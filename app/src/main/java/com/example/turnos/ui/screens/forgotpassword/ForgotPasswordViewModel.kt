package com.example.turnos.ui.screens.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.repository.AuthRepository
import com.example.turnos.data.repository.WrongSecurityAnswerException
import com.example.turnos.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Recuperación en tres pasos: usuario → pregunta de seguridad → nueva contraseña.
 * Después de 3 respuestas incorrectas se bloquea y el cliente debe pedir al local
 * que le restablezca la contraseña (pantalla Clientes del negocio).
 */

class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.OnUsernameChange -> _state.update { it.copy(username = event.value, errorMessage = null) }
            is ForgotPasswordEvent.OnAnswerChange -> _state.update { it.copy(answer = event.value, errorMessage = null) }
            is ForgotPasswordEvent.OnNewPasswordChange -> _state.update { it.copy(newPassword = event.value, passwordError = null) }
            is ForgotPasswordEvent.OnConfirmPasswordChange -> _state.update { it.copy(confirmPassword = event.value, confirmError = null) }
            ForgotPasswordEvent.OnTogglePasswordVisibility -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            ForgotPasswordEvent.OnContinueClick -> when (_state.value.step) {
                RecoveryStep.USERNAME -> findQuestion()
                RecoveryStep.SECURITY_QUESTION -> checkAnswer()
                RecoveryStep.NEW_PASSWORD -> savePassword()
                RecoveryStep.DONE -> Unit
            }
            ForgotPasswordEvent.OnStepBackClick -> _state.update {
                when (it.step) {
                    RecoveryStep.SECURITY_QUESTION -> it.copy(step = RecoveryStep.USERNAME, answer = "", errorMessage = null)
                    RecoveryStep.NEW_PASSWORD -> it.copy(step = RecoveryStep.SECURITY_QUESTION, newPassword = "", confirmPassword = "")
                    else -> it
                }
            }
            ForgotPasswordEvent.OnBackToLoginClick -> Unit
        }
    }

    private fun findQuestion() {
        val username = _state.value.username
        if (username.isBlank()) {
            _state.update { it.copy(errorMessage = "Escribe tu usuario.") }
            return
        }
        launchLoading {
            authRepository.getSecurityQuestion(username)
                .onSuccess { question ->
                    _state.update { it.copy(step = RecoveryStep.SECURITY_QUESTION, securityQuestion = question) }
                }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    private fun checkAnswer() {
        val s = _state.value
        if (s.isLocked) return
        if (!Validators.isValidSecurityAnswer(s.answer)) {
            _state.update { it.copy(errorMessage = "Escribe tu respuesta.") }
            return
        }
        launchLoading {
            authRepository.verifySecurityAnswer(s.username, s.answer)
                .onSuccess { _state.update { it.copy(step = RecoveryStep.NEW_PASSWORD) } }
                .onFailure { e ->
                    if (e is WrongSecurityAnswerException) {
                        val left = s.attemptsLeft - 1
                        _state.update {
                            it.copy(
                                attemptsLeft = left,
                                errorMessage = if (left > 0) "Respuesta incorrecta. Te quedan $left intentos."
                                else "Demasiados intentos. Pide al local que restablezca tu contraseña.",
                            )
                        }
                    } else {
                        _state.update { it.copy(errorMessage = e.message) }
                    }
                }
        }
    }

    private fun savePassword() {
        val s = _state.value
        val passwordError = if (!Validators.isValidPassword(s.newPassword)) "Mínimo ${Validators.MIN_PASSWORD_LENGTH} caracteres" else null
        val confirmError = if (s.confirmPassword != s.newPassword) "Las contraseñas no coinciden" else null
        if (passwordError != null || confirmError != null) {
            _state.update { it.copy(passwordError = passwordError, confirmError = confirmError) }
            return
        }
        launchLoading {
            authRepository.resetPasswordWithAnswer(s.username, s.answer, s.newPassword)
                .onSuccess { _state.update { it.copy(step = RecoveryStep.DONE, answer = "", newPassword = "", confirmPassword = "") } }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    private fun launchLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            block()
            _state.update { it.copy(isLoading = false) }
        }
    }
}
