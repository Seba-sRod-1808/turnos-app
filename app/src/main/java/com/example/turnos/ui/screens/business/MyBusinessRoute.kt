package com.example.turnos.ui.screens.business

import androidx.compose.runtime.*
import com.example.turnos.data.source.FakeDataSource
import kotlinx.coroutines.delay

@Composable
fun MyBusinessRoute(
    onSectionClick: (AdminSection) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var state by remember { mutableStateOf(MyBusinessUiState(isLoading = true)) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        state = state.copy(isLoading = true, errorMessage = null)
        delay(500) // carga simulada
        state = state.copy(isLoading = false, business = FakeDataSource.business)
    }

    MyBusinessScreen(
        state = state,
        onSectionClick = onSectionClick,
        onEditProfile = { },
        onRetry = { reloadKey++ },
        onLogout = onLogout,
    )
}
