package com.example.turnos.ui.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.model.PaymentMethod
import com.example.turnos.data.source.FakeDataSource
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

/** Resumen de cobros del mes (tarjeta oscura del diseño). */
data class RevenueSummary(
    val monthLabel: String,
    val total: Int,
    val deltaPct: Int,
    val count: Int,
    val nextDeposit: Int,
)

data class PaymentsUiState(
    val summary: RevenueSummary? = null,
    val methods: List<PaymentMethod> = emptyList(),
) {
    val enabledCount: Int get() = methods.count { it.enabled }
}

@Composable
fun PaymentMethodsScreen(
    state: PaymentsUiState,
    onBack: () -> Unit,
    onToggleMethod: (PaymentMethod, Boolean) -> Unit,
    onConfigureDeposit: () -> Unit,
) {
    TurnosScaffold(
        title = "Métodos de Pago",
        onBack = onBack,
        actionIcon = Icons.Outlined.Settings,
        actionDescription = "Ajustes de pago",
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            state.summary?.let { item { RevenueCard(it) } }

            item {
                SectionHeader(
                    "Métodos habilitados",
                    "${state.enabledCount} de ${state.methods.size} activos",
                    Modifier.padding(top = 6.dp),
                )
            }
            if (state.enabledCount == 0 && state.methods.isNotEmpty()) {
                item {
                    InfoBanner(
                        "No tienes métodos activos: tus clientes no podrán pagar desde la app.",
                        icon = Icons.Outlined.ErrorOutline,
                        fg = DangerRed, bg = DangerSoft, border = DangerRed.copy(alpha = 0.4f),
                    )
                }
            }
            items(state.methods, key = { it.id }) { m ->
                PaymentMethodCard(m, onToggle = { onToggleMethod(m, it) })
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
                                modifier = Modifier.clickable(onClick = onConfigureDeposit).padding(6.dp),
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
        "p2" -> Icons.Outlined.CreditCard
        "p3" -> Icons.Outlined.AccountBalance
        "p4" -> Icons.Outlined.Link
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Cobros de ${s.monthLabel}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
            Row(
                Modifier.clip(MaterialTheme.shapes.small).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Este mes", style = MaterialTheme.typography.labelMedium, color = Color.White)
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(quetzales(s.total), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${s.deltaPct}% vs. mes anterior", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4ADE80))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(s.count.toString(), style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text("cobros", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewRevenue = RevenueSummary("junio", 4860, 12, 34, 1420)

@Composable
private fun PaymentsPreviewHost(state: PaymentsUiState) = TurnosTheme {
    PaymentMethodsScreen(state, {}, { _, _ -> }, {})
}

@Preview(showBackground = true, heightDp = 1000, name = "Pagos - configurado")
@Composable
private fun PaymentsPreview() = PaymentsPreviewHost(PaymentsUiState(previewRevenue, FakeDataSource.paymentMethods))

@Preview(showBackground = true, heightDp = 1000, name = "Pagos - todos desactivados (advertencia)")
@Composable
private fun PaymentsNoneEnabledPreview() = PaymentsPreviewHost(
    PaymentsUiState(previewRevenue, FakeDataSource.paymentMethods.map { it.copy(enabled = false) }),
)
