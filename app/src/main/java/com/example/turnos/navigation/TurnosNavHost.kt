package com.example.turnos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.turnos.ui.screens.auth.LoginRoute
import com.example.turnos.ui.screens.auth.UserRole
import com.example.turnos.ui.screens.availability.AvailabilityRoute
import com.example.turnos.ui.screens.booking.BookingRoute
import com.example.turnos.ui.screens.business.AdminSection
import com.example.turnos.ui.screens.business.MyBusinessRoute
import com.example.turnos.ui.screens.catalog.CatalogRoute
import com.example.turnos.ui.screens.history.HistoryRoute
import com.example.turnos.ui.screens.notifications.NotificationsRoute
import com.example.turnos.ui.screens.payments.PaymentMethodsRoute
import com.example.turnos.ui.screens.services.ServicesRoute
import com.example.turnos.ui.screens.team.TeamRoute

/**
 * Navegación básica entre las Routes (opcional en esta entrega: cada
 * pantalla también puede revisarse de forma aislada con sus @Preview).
 */
@Composable
fun TurnosNavHost(navController: NavHostController = rememberNavController()) {
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Destinations.LOGIN) {

        composable(Destinations.LOGIN) {
            LoginRoute(onLoginSuccess = { role ->
                val dest = if (role == UserRole.BUSINESS) Destinations.MY_BUSINESS else Destinations.CATALOG
                navController.navigate(dest) { popUpTo(Destinations.LOGIN) { inclusive = true } }
            })
        }

        // ---------- Cliente ----------
        composable(Destinations.CATALOG) {
            CatalogRoute(
                onReserve = { navController.navigate(Destinations.booking(it)) },
                onOpenHistory = { navController.navigate(Destinations.CLIENT_HISTORY) { launchSingleTop = true } },
                onLogout = { logout(navController) },
            )
        }
        composable(
            Destinations.BOOKING,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType }),
        ) { entry ->
            BookingRoute(
                serviceId = entry.arguments?.getString("serviceId").orEmpty(),
                onBack = back,
                onViewAgenda = { navController.navigate(Destinations.CLIENT_HISTORY) { popUpTo(Destinations.CATALOG) } },
            )
        }
        composable(Destinations.CLIENT_HISTORY) {
            HistoryRoute(isBusiness = false, onBack = back, onGoToCatalog = back, onLogout = { logout(navController) })
        }

        // ---------- Negocio ----------
        composable(Destinations.MY_BUSINESS) {
            MyBusinessRoute(
                onSectionClick = { section ->
                    navController.navigate(
                        when (section) {
                            AdminSection.SERVICES -> Destinations.SERVICES
                            AdminSection.AVAILABILITY -> Destinations.AVAILABILITY
                            AdminSection.TEAM -> Destinations.TEAM
                            AdminSection.HISTORY -> Destinations.BUSINESS_HISTORY
                            AdminSection.PAYMENTS -> Destinations.PAYMENTS
                            AdminSection.NOTIFICATIONS -> Destinations.NOTIFICATIONS
                        }
                    )
                },
                onLogout = { logout(navController) },
            )
        }
        composable(Destinations.SERVICES) { ServicesRoute(onBack = back) }
        composable(Destinations.AVAILABILITY) { AvailabilityRoute(onBack = back) }
        composable(Destinations.TEAM) { TeamRoute(onBack = back) }
        composable(Destinations.BUSINESS_HISTORY) { HistoryRoute(isBusiness = true, onBack = back) }
        composable(Destinations.PAYMENTS) { PaymentMethodsRoute(onBack = back) }
        composable(Destinations.NOTIFICATIONS) { NotificationsRoute(onBack = back) }
    }
}

private fun logout(navController: NavHostController) {
    navController.navigate(Destinations.LOGIN) { popUpTo(0) { inclusive = true } }
}
