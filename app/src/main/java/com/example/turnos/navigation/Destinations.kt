package com.example.turnos.navigation

/** Destinos de navegación. Separados por rol: cliente y negocio (administrador). */
object Destinations {
    const val LOGIN = "login"

    // Cliente
    const val CATALOG = "catalog"
    const val BOOKING = "booking/{serviceId}"
    fun booking(serviceId: String) = "booking/$serviceId"
    const val CLIENT_HISTORY = "client_history"

    // Negocio
    const val MY_BUSINESS = "my_business"
    const val SERVICES = "services"
    const val AVAILABILITY = "availability"
    const val TEAM = "team"
    const val BUSINESS_HISTORY = "business_history"
    const val PAYMENTS = "payments"
    const val NOTIFICATIONS = "notifications"
}
