package com.example.turnos.ui.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnos.data.mock.MockData
import com.example.turnos.ui.components.*
import com.example.turnos.ui.theme.*

@Composable
fun PaymentMethodsScreen(onBack: () -> Unit) {
    val methods = remember { mutableStateListOf(*MockData.paymentMethods.toTypedArray()) }
    val icons = mapOf(
        "p1" to Icons.Outlined.Payments,
        "p2" to Icons.Outlined.CreditCard,
        "p3" to Icons.Outlined.AccountBalance,
        "p4" to Icons.Outlined.Link,
    )

    TurnosScaffold(
        title = "Métodos de Pago",
        onBack = onBack,
        actionIcon = Icons.Outlined.Settings,
        actionDescription = "Ajustes de pago",
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RevenueCard(total = 4860, deltaPct = 12, count = 34)

            SectionHeader("Métodos habilitados", "${methods.count { it.enabled }} de ${methods.size} activos", Modifier.padding(top = 6.dp))
            methods.forEachIndexed { i, m ->
                TurnosCard(padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(icons[m.id] ?: Icons.Outlined.Payments)
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
                        TurnosSwitch(m.enabled) { methods[i] = m.copy(enabled = it) }
                    }
                }
            }

            TurnosCard(padding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Depósito automático", style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text("Cada lunes · Próximo: ${quetzales(1420)}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    Text(
                        "Configurar",
                        style = MaterialTheme.typography.labelMedium,
                        color = TurnosOrange,
                        modifier = Modifier.clickable { }.padding(6.dp),
                    )
                }
            }

            InfoBanner("Los pagos con tarjeta se liquidan en 1–2 días hábiles.", icon = Icons.Outlined.VerifiedUser)
        }
    }
}

@Composable
private fun RevenueCard(total: Int, deltaPct: Int, count: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(DarkCard)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Cobros de junio", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
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
                Text(quetzales(total), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$deltaPct% vs. mayo", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4ADE80))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(count.toString(), style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text("cobros", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentsPreview() = TurnosTheme { PaymentMethodsScreen {} }
