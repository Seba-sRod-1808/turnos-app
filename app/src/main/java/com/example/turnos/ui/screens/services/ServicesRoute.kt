package com.example.turnos.ui.screens.services

import androidx.compose.runtime.*
import com.example.turnos.data.model.Service
import com.example.turnos.data.source.FakeDataSource

@Composable
fun ServicesRoute(onBack: () -> Unit = {}) {
    var state by remember { mutableStateOf(ServicesUiState(services = FakeDataSource.services)) }

    fun replace(updated: Service) {
        state = state.copy(services = state.services.map { if (it.id == updated.id) updated else it })
    }

    ServicesScreen(
        state = state,
        onBack = onBack,
        onToggleActive = { s, on -> replace(s.copy(active = on, paused = !on)) },
        onEditClick = { s ->
            state = state.copy(
                form = ServiceForm(s.id, s.name, s.description, s.durationMin.toString(), s.price.toString()),
            )
        },
        onAddClick = { state = state.copy(form = ServiceForm()) },
        onFormChange = { state = state.copy(form = it) },
        onFormSave = {
            val f = state.form ?: return@ServicesScreen
            if (f.isNew) {
                val new = Service(
                    id = "s${System.currentTimeMillis()}",
                    name = f.name.trim(),
                    description = f.description.trim(),
                    durationMin = f.duration.toInt(),
                    price = f.price.toInt(),
                )
                state = state.copy(services = state.services + new, form = null)
            } else {
                state.services.firstOrNull { it.id == f.id }?.let { old ->
                    replace(
                        old.copy(
                            name = f.name.trim(),
                            description = f.description.trim(),
                            durationMin = f.duration.toInt(),
                            price = f.price.toInt(),
                        )
                    )
                }
                state = state.copy(form = null)
            }
        },
        onFormDelete = {
            val id = state.form?.id
            state = state.copy(services = state.services.filterNot { it.id == id }, form = null)
        },
        onFormDismiss = { state = state.copy(form = null) },
    )
}
