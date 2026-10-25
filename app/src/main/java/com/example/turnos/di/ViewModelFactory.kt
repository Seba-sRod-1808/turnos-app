package com.example.turnos.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.turnos.TurnosApp

/**
 * Crea un ViewModel con acceso al [AppContainer] y al [SavedStateHandle]
 * (de donde se leen los argumentos type-safe de la navegación).
 *
 * Uso: `val vm = turnosViewModel { handle -> BookingViewModel(handle, appointmentRepository, …) }`
 */

@Composable
inline fun <reified VM : ViewModel> turnosViewModel(
    crossinline create: AppContainer.(SavedStateHandle) -> VM,
): VM = viewModel {
    val app = this[APPLICATION_KEY] as TurnosApp
    app.container.create(createSavedStateHandle())
}
