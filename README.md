# Turnos — Entrega: vistas en Jetpack Compose

App de reservas para negocios locales (barberías). Proyecto de Programación de Plataformas Móviles, UVG.
Esta entrega implementa **todas las vistas a nivel visual**: Kotlin + Jetpack Compose + Material 3, con fuente de datos fake y sin backend.

## Funcionalidades

Se implementan todas las funcionalidades del diseño en esta entrega:

| Funcionalidad | Pantallas |
|---|---|
| Autenticación y perfiles | Login |
| Reserva autónoma de citas | Catálogo, Confirmación de cita |
| Historial y estado de citas | Historial (cliente y negocio) |
| Gestión del catálogo de servicios | Mi Local, Servicios y Precios |
| Control dinámico de disponibilidad | Control de Disponibilidad |
| Gestión del equipo | Equipo / Barberos |
| Métodos de pago | Métodos de Pago |
| Notificaciones y recordatorios | Notificaciones |

Quedan para fases posteriores, porque dependen de backend o de APIs externas: el mapa real (Google Maps), el inicio de sesión con Google, el envío real de notificaciones (FCM) y el modo sin conexión (Room).

## Patrón Route / Screen

Cada pantalla está dividida en dos archivos:

- **`XxxRoute.kt`**: composable con estado. Guarda el `UiState`, lee de `FakeDataSource` y maneja los eventos.
- **`XxxScreen.kt`**: composable **stateless**. Recibe un `XxxUiState` y lambdas de eventos, y solo dibuja la UI. Aquí están los `@Preview`, uno por cada estado de la pantalla.

| Pantalla | Estados con preview |
|---|---|
| Login | vacío, llenando, cargando, correo inválido, credenciales incorrectas |
| Catálogo | con servicios, buscando, búsqueda sin resultados, cargando, error |
| Confirmación de cita | sin horario, horario elegido, confirmando, confirmada, horario ya tomado, día sin horarios |
| Historial | próximas, pasadas, diálogo de cancelar, vista de negocio, sin citas, cargando |
| Mi Local | contenido, cargando, error |
| Servicios y Precios | lista, vacío, nuevo servicio, editar servicio |
| Disponibilidad | configurada, guardando, sin días (error), sin bloques (error) |
| Equipo | con barberos, vacío, cargando |
| Métodos de Pago | configurado, todos desactivados (advertencia) |
| Notificaciones | configuradas, sin plantillas, editando plantilla |

## Estructura

```
app/src/main/java/com/example/turnos/
├── MainActivity.kt
├── navigation/                  # Navegación básica entre Routes (opcional en esta entrega)
│   ├── Destinations.kt
│   └── TurnosNavHost.kt
├── data/
│   ├── model/Models.kt          # data classes: Service, Barber, Appointment, TimeSlot…
│   └── source/FakeDataSource.kt # Datos fake que alimentan las listas Lazy
└── ui/
    ├── theme/                   # Colores, tipografía y tema
    ├── components/              # Componentes reutilizables + LoadingContent / EmptyContent / ErrorContent
    └── screens/
        ├── auth/          LoginRoute.kt          · LoginScreen.kt
        ├── catalog/       CatalogRoute.kt        · CatalogScreen.kt
        ├── booking/       BookingRoute.kt        · BookingConfirmationScreen.kt
        ├── history/       HistoryRoute.kt        · HistoryScreen.kt
        ├── business/      MyBusinessRoute.kt     · MyBusinessScreen.kt
        ├── services/      ServicesRoute.kt       · ServicesScreen.kt
        ├── availability/  AvailabilityRoute.kt   · AvailabilityScreen.kt
        ├── team/          TeamRoute.kt           · TeamScreen.kt
        ├── payments/      PaymentMethodsRoute.kt · PaymentMethodsScreen.kt
        └── notifications/ NotificationsRoute.kt  · NotificationsScreen.kt
```

## Probar la app

Credenciales de prueba: **demo@turnos.com / 123456**. Con “Soy cliente” se entra al catálogo; con “Soy negocio” se entra a Mi Local. Cualquier otra combinación muestra el estado de error.
