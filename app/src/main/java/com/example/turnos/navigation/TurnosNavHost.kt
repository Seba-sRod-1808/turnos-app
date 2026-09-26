package com.example.turnos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.turnos.ui.screens.auth.LoginScreen
import com.example.turnos.ui.screens.auth.UserRole
import com.example.turnos.ui.screens.availability.AvailabilityScreen
import com.example.turnos.ui.screens.booking.BookingConfirmationScreen
import com.example.turnos.ui.screens.business.MyBusinessScreen
import com.example.turnos.ui.screens.catalog.CatalogScreen
import com.example.turnos.ui.screens.history.HistoryScreen
import com.example.turnos.ui.screens.notifications.NotificationsScreen
import com.example.turnos.ui.screens.payments.PaymentMethodsScreen
import com.example.turnos.ui.screens.services.ServicesScreen
import com.example.turnos.ui.screens.team.TeamScreen

@Composable
fun TurnosNavHost(navController: NavHostController = rememberNavController()) {
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(onLogin = { role ->
                val dest = if (role == UserRole.BUSINESS) Routes.MY_BUSINESS else Routes.CATALOG
                navController.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
            })
        }

        composable(Routes.CATALOG) {
            CatalogScreen(
                onReserve = { navController.navigate(Routes.booking(it)) },
                onOpenHistory = { navController.navigate(Routes.CLIENT_HISTORY) { launchSingleTop = true } },
                onLogout = { logout(navController) },
            )
        }
        composable(
            Routes.BOOKING,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType }),
        ) { entry ->
            BookingConfirmationScreen(
                serviceId = entry.arguments?.getString("serviceId").orEmpty(),
                onBack = back,
                onViewAgenda = {
                    navController.navigate(Routes.CLIENT_HISTORY) { popUpTo(Routes.CATALOG) }
                },
            )
        }
        composable(Routes.CLIENT_HISTORY) {
            HistoryScreen(isBusiness = false, onBack = back, onLogout = { logout(navController) })
        }

        composable(Routes.MY_BUSINESS) {
            MyBusinessScreen(
                onNavigate = { navController.navigate(it) },
                onLogout = { logout(navController) },
            )
        }
        composable(Routes.SERVICES) { ServicesScreen(onBack = back) }
        composable(Routes.AVAILABILITY) { AvailabilityScreen(onBack = back) }
        composable(Routes.TEAM) { TeamScreen(onBack = back) }
        composable(Routes.BUSINESS_HISTORY) { HistoryScreen(isBusiness = true, onBack = back) }
        composable(Routes.PAYMENTS) { PaymentMethodsScreen(onBack = back) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(onBack = back) }
    }
}

private fun logout(navController: NavHostController) {
    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
}
