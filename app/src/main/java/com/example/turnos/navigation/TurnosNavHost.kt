package com.example.turnos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.turnos.data.model.UserRole
import com.example.turnos.ui.screens.availability.AvailabilityRoute
import com.example.turnos.ui.screens.booking.BookingRoute
import com.example.turnos.ui.screens.business.AdminSection
import com.example.turnos.ui.screens.business.MyBusinessRoute
import com.example.turnos.ui.screens.catalog.CatalogRoute
import com.example.turnos.ui.screens.history.HistoryRoute
import com.example.turnos.ui.screens.login.LoginRoute
import com.example.turnos.ui.screens.notifications.NotificationsRoute
import com.example.turnos.ui.screens.payments.PaymentsRoute
import com.example.turnos.ui.screens.register.RegisterRoute
import com.example.turnos.ui.screens.services.ServicesRoute
import com.example.turnos.ui.screens.team.TeamRoute

@Composable
fun TurnosNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AuthGraph) {
        authGraph(navController)
        clientGraph(navController)
        businessGraph(navController)
    }
}

// ---------------------------------------------------------------------------
// Autenticación
// ---------------------------------------------------------------------------

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation<AuthGraph>(startDestination = LoginDestination) {
        composable<LoginDestination> {
            LoginRoute(
                onLoggedIn = { role -> navController.navigateToHome(role) },
                onCreateAccount = { navController.navigate(RegisterDestination) },
            )
        }
        composable<RegisterDestination> {
            RegisterRoute(
                onRegistered = { role -> navController.navigateToHome(role) },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Cliente
// ---------------------------------------------------------------------------

private fun NavGraphBuilder.clientGraph(navController: NavHostController) {
    navigation<ClientGraph>(startDestination = CatalogDestination) {
        composable<CatalogDestination> {
            CatalogRoute(
                onReserve = { serviceId -> navController.navigate(BookingDestination(serviceId)) },
                onMyAppointments = {
                    navController.navigate(HistoryDestination(asBusiness = false)) { launchSingleTop = true }
                },
                onLoggedOut = { navController.logout() },
            )
        }
        composable<BookingDestination> {
            BookingRoute(
                onBack = { navController.popBackStack() },
                onViewAgenda = {
                    // Al ver la agenda, la reserva sale del back stack.
                    navController.navigate(HistoryDestination(asBusiness = false)) {
                        popUpTo<CatalogDestination>()
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<HistoryDestination> {
            HistoryRoute(
                onBack = { navController.popBackStack() },
                onReschedule = { serviceId, appointmentId ->
                    navController.navigate(BookingDestination(serviceId, rescheduleAppointmentId = appointmentId))
                },
                onGoToCatalog = {
                    navController.navigate(CatalogDestination) {
                        popUpTo<CatalogDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoggedOut = { navController.logout() },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Negocio
// ---------------------------------------------------------------------------

private fun NavGraphBuilder.businessGraph(navController: NavHostController) {
    val back: () -> Unit = { navController.popBackStack() }

    navigation<BusinessGraph>(startDestination = BusinessHomeDestination) {
        composable<BusinessHomeDestination> {
            MyBusinessRoute(
                onSectionClick = { section -> navController.navigate(section.destination()) },
                onLoggedOut = { navController.logout() },
            )
        }
        composable<ServicesDestination> { ServicesRoute(onBack = back) }
        composable<AvailabilityDestination> { AvailabilityRoute(onBack = back) }
        composable<TeamDestination> {
            TeamRoute(
                onBack = back,
                onViewAgenda = { navController.navigate(HistoryDestination(asBusiness = true)) },
            )
        }
        composable<PaymentsDestination> { PaymentsRoute(onBack = back) }
        composable<NotificationsDestination> { NotificationsRoute(onBack = back) }
        // HistoryDestination se registra una sola vez (en el grafo del cliente) y
        // es accesible desde cualquier grafo; el argumento asBusiness decide qué ve.
    }
}

/** Cada sección del panel apunta a su destino type-safe. */
private fun AdminSection.destination(): Any = when (this) {
    AdminSection.SERVICES -> ServicesDestination
    AdminSection.AVAILABILITY -> AvailabilityDestination
    AdminSection.TEAM -> TeamDestination
    AdminSection.HISTORY -> HistoryDestination(asBusiness = true)
    AdminSection.PAYMENTS -> PaymentsDestination
    AdminSection.NOTIFICATIONS -> NotificationsDestination
}

/** Entra al inicio según el rol y saca la autenticación del back stack. */
private fun NavHostController.navigateToHome(role: UserRole) {
    val graph: Any = if (role == UserRole.BUSINESS) BusinessGraph else ClientGraph
    navigate(graph) {
        popUpTo<AuthGraph> { inclusive = true }
        launchSingleTop = true
    }
}

/** Cierra sesión: limpia todo el back stack y vuelve al login. */
private fun NavHostController.logout() {
    navigate(AuthGraph) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
