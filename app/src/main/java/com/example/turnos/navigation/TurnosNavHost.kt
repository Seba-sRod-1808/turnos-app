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
import com.example.turnos.ui.screens.clients.ClientsRoute
import com.example.turnos.ui.screens.forgotpassword.ForgotPasswordRoute
import com.example.turnos.ui.screens.history.HistoryRoute
import com.example.turnos.ui.screens.login.LoginRoute
import com.example.turnos.ui.screens.notifications.NotificationsRoute
import com.example.turnos.ui.screens.payments.PaymentsRoute
import com.example.turnos.ui.screens.services.ServicesRoute
import com.example.turnos.ui.screens.setupaccount.SetupAccountRoute
import com.example.turnos.ui.screens.team.TeamRoute

/**
 * Grafo de navegación. HistoryDestination se registra una sola vez (en el grafo del cliente)
 * y es accesible desde cualquier grafo; sus argumentos deciden qué citas muestra.
 * Al abrir la agenda desde una reserva, la reserva sale del back stack.
 * El primer ingreso y el inicio de sesión sacan todo el grafo de autenticación del back stack.
 */

@Composable
fun TurnosNavHost(
    startDestination: Any,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = startDestination) {
        authGraph(navController)
        clientGraph(navController)
        businessGraph(navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation<AuthGraph>(startDestination = LoginDestination) {
        composable<LoginDestination> {
            LoginRoute(
                onLoggedIn = { role -> navController.navigateToHome(role) },
                onFirstLogin = { userId -> navController.navigate(SetupAccountDestination(userId)) },
                onForgotPassword = { navController.navigate(ForgotPasswordDestination) },
            )
        }
        composable<ForgotPasswordDestination> {
            ForgotPasswordRoute(onBackToLogin = { navController.popBackStack() })
        }
        composable<SetupAccountDestination> {
            SetupAccountRoute(
                onCompleted = { role -> navController.navigateToHome(role) },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}

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

private fun NavGraphBuilder.businessGraph(navController: NavHostController) {
    val back: () -> Unit = { navController.popBackStack() }

    navigation<BusinessGraph>(startDestination = BusinessHomeDestination) {
        composable<BusinessHomeDestination> {
            MyBusinessRoute(
                onSectionClick = { section -> navController.navigate(section.destination()) },
                onLoggedOut = { navController.logout() },
            )
        }
        composable<ClientsDestination> { ClientsRoute(onBack = back) }
        composable<ServicesDestination> { ServicesRoute(onBack = back) }
        composable<AvailabilityDestination> { AvailabilityRoute(onBack = back) }
        composable<TeamDestination> {
            TeamRoute(
                onBack = back,
                onViewAgenda = { barberId -> navController.navigate(HistoryDestination(asBusiness = true, barberId = barberId)) },
            )
        }
        composable<PaymentsDestination> { PaymentsRoute(onBack = back) }
        composable<NotificationsDestination> { NotificationsRoute(onBack = back) }
    }
}

private fun AdminSection.destination(): Any = when (this) {
    AdminSection.CLIENTS -> ClientsDestination
    AdminSection.SERVICES -> ServicesDestination
    AdminSection.AVAILABILITY -> AvailabilityDestination
    AdminSection.TEAM -> TeamDestination
    AdminSection.HISTORY -> HistoryDestination(asBusiness = true)
    AdminSection.PAYMENTS -> PaymentsDestination
    AdminSection.NOTIFICATIONS -> NotificationsDestination
}

private fun NavHostController.navigateToHome(role: UserRole) {
    navigate(homeGraphFor(role)) {
        popUpTo<AuthGraph> { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.logout() {
    navigate(AuthGraph) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
