package com.example.turnos.ui.screens.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.repository.AppointmentRepository
import com.example.turnos.data.repository.SettingsRepository
import com.example.turnos.util.formatMonth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

/**
 * Ingresos = citas completadas. Se compara el mes actual contra el anterior.
 */

class PaymentsViewModel(
    private val settingsRepository: SettingsRepository,
    appointmentRepository: AppointmentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentsState())
    val state: StateFlow<PaymentsState> = _state.asStateFlow()

    init {
        combine(settingsRepository.observePaymentMethods(), appointmentRepository.observeAll()) { methods, appointments ->
            methods to buildSummary(appointments)
        }
            .onEach { (methods, summary) -> _state.update { it.copy(isLoading = false, methods = methods, summary = summary) } }
            .catch { _state.update { it.copy(isLoading = false, userMessage = "No se pudo cargar la información de pagos.") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: PaymentsEvent) {
        when (event) {
            is PaymentsEvent.OnToggleMethod -> viewModelScope.launch {
                settingsRepository.setPaymentEnabled(event.method.id, event.enabled)
            }
            PaymentsEvent.OnConfigureDepositClick ->
                _state.update { it.copy(userMessage = "El depósito automático se configurará con la pasarela de pagos.") }
            PaymentsEvent.OnMessageShown -> _state.update { it.copy(userMessage = null) }
            PaymentsEvent.OnBackClick -> Unit
        }
    }

    private fun buildSummary(appointments: List<Appointment>): RevenueSummary {
        val completed = appointments.filter { it.status == AppointmentStatus.COMPLETED }
        val thisMonth = YearMonth.now()
        val current = completed.filter { YearMonth.from(it.date) == thisMonth }
        val previousTotal = completed.filter { YearMonth.from(it.date) == thisMonth.minusMonths(1) }.sumOf { it.price }
        val total = current.sumOf { it.price }
        val weekAgo = LocalDate.now().minusDays(7)
        return RevenueSummary(
            monthLabel = formatMonth(LocalDate.now()),
            total = total,
            deltaPct = if (previousTotal == 0) null else ((total - previousTotal) * 100.0 / previousTotal).roundToInt(),
            count = current.size,
            nextDeposit = completed.filter { it.date.isAfter(weekAgo) }.sumOf { it.price },
        )
    }
}
