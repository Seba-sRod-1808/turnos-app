package com.example.turnos.ui.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnos.data.model.PaymentMethod
import com.example.turnos.di.turnosViewModel
import com.example.turnos.ui.components.*
import com.example.turnos.ui.preview.PreviewData
import com.example.turnos.ui.theme.*
import com.example.turnos.util.quetzales

@Composable
fun PaymentsRoute(
    onBack: () -> Unit,
    viewModel: PaymentsViewModel = turnosViewModel { PaymentsViewModel(settingsRepository, appointmentRepository) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    UserMessageEffect(state.userMessage, snackbar) { viewModel.onEvent(PaymentsEvent.OnMessageShown) }

    PaymentsScreen(state, snackbar) { event ->
        when (event) {
            PaymentsEvent.OnBackClick -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun PaymentsScreen(
    state: PaymentsState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (PaymentsEvent) -> Unit,
) {
    TurnosScaffold(
        title = "Métodos de Pago",
        onBack = { onEvent(PaymentsEvent.OnBackClick) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        if (state.isLoading) {
            LoadingContent(Modifier.padding(padding))
            return@TurnosScaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            state.summary?.let { summary -> item { RevenueCard(summary) } }

            item {
                SectionHeader("Métodos habilitados", "${state.enabledCount} de ${state.methods.size} activos", Modifier.padding(top = 6.dp))
            }
            if (state.enabledCount == 0 && state.methods.isNotEmpty()) {
                item { ErrorBanner("No tienes métodos activos: tus clientes no podrán pagar desde la app.") }
            }
            items(state.methods, key = { it.id }) { m ->
                PaymentMethodCard(m) { onEvent(PaymentsEvent.OnToggleMethod(m, it)) }
            }

            state.summary?.let { s ->
                item {
                    TurnosCard(padding = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Depósito automático", style = MaterialTheme.typography.titleSmall, color = Ink)
                                Text("Cada lunes · Próximo: ${quetzales(s.nextDeposit)}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                            Text(
                                "Configurar",
                                style = MaterialTheme.typography.labelMedium,
                                color = TurnosOrange,
                                modifier = Modifier.clickable { onEvent(PaymentsEvent.OnConfigureDepositClick) }.padding(6.dp),
                            )
                        }
                    }
                }
            }
            item { InfoBanner("Los pagos con tarjeta se liquidan en 1–2 días hábiles.", icon = Icons.Outlined.VerifiedUser) }
        }
    }
}

@Composable
private fun PaymentMethodCard(m: PaymentMethod, onToggle: (Boolean) -> Unit) {
    val icon = when (m.id) {
        "card" -> Icons.Outlined.CreditCard
        "transfer" -> Icons.Outlined.AccountBalance
        "link" -> Icons.Outlined.Link
        else -> Icons.Outlined.Payments
    }
    TurnosCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(m.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                    if (m.connected) {
                        Spacer(Modifier.width(6.dp))
                        StatusChip("Conectado", SuccessGreen, SuccessSoft)
                    }
                }
                Text(m.detail, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            }
            TurnosSwitch(m.enabled, onToggle)
        }
    }
}

@Composable
private fun RevenueCard(s: RevenueSummary) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(DarkCard)
            .padding(16.dp),
    ) {
        Text("Cobros de ${s.monthLabel}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(quetzales(s.total), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                val delta = s.deltaPct
                if (delta != null) {
                    val up = delta >= 0
                    val color = if (up) Color(0xFF4ADE80) else Color(0xFFF87171)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (up) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            null, tint = color, modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${if (up) "+" else ""}$delta% vs. mes anterior", style = MaterialTheme.typography.bodySmall, color = color)
                    }
                } else {
                    Text("Primer mes con cobros", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(s.count.toString(), style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text("cobros", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

private val sample = PaymentsState(
    summary = RevenueSummary("junio", 4860, 12, 34, 1420),
    methods = PreviewData.paymentMethods,
    isLoading = false,
)

@Composable
private fun Host(state: PaymentsState) = TurnosTheme { PaymentsScreen(state) {} }

@Preview(showBackground = true, heightDp = 1000, name = "Pagos - configurado")
@Composable
private fun PaymentsPreview() = Host(sample)

@Preview(showBackground = true, heightDp = 1000, name = "Pagos - todos desactivados")
@Composable
private fun PaymentsNoneEnabledPreview() = Host(sample.copy(methods = PreviewData.paymentMethods.map { it.copy(enabled = false) }))

@Preview(showBackground = true, name = "Pagos - cargando")
@Composable
private fun PaymentsLoadingPreview() = Host(PaymentsState(isLoading = true))
