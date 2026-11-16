# Turnos

**Turnos** es una app Android para gestionar citas en negocios locales, empezando por barberías. Los clientes reservan, reprograman o cancelan sus citas en tiempo real según la disponibilidad exacta del local, sin llamadas ni filas. El dueño controla su agenda, sus servicios, sus horarios, su equipo y las cuentas de sus clientes desde la misma app.

Proyecto de Programación de Plataformas Móviles, Universidad del Valle de Guatemala.
Integrantes: Fabricio Estrada (25230), Sebastian Rodas (25038) y Mauricio Corado (25218).

El APK de la entrega está en la carpeta [`APK/`](APK/).

---

## Cómo probarla

La primera vez que se abre, la app carga datos de demostración.

| Rol | Usuario | Contraseña | Qué muestra |
|---|---|---|---|
| Negocio | `negocio` | `123456` | Panel del local |
| Cliente | `cliente` | `123456` | Catálogo, reservas e historial |
| Cliente nuevo | `pedro` | `temporal` | Flujo de primer ingreso (cambiar contraseña) |

Para recuperar la contraseña de `cliente` o `negocio`, la respuesta de seguridad es `Guatemala`.

---

## Funcionalidades

| Funcionalidad del documento | Cómo está implementada |
|---|---|
| Autenticación y perfiles | Login con usuario y contraseña. El dueño crea las cuentas de sus clientes con una contraseña temporal, y en el primer ingreso el cliente define su contraseña y una pregunta de seguridad. La sesión se guarda en el teléfono. |
| Recuperar contraseña | El cliente responde su pregunta de seguridad (máximo 3 intentos). Si no puede, el dueño le genera una contraseña temporal desde **Clientes y accesos**. |
| Gestión del catálogo | El dueño agrega, edita, pausa y elimina servicios. No se puede eliminar un servicio con citas pendientes. |
| Disponibilidad dinámica | Días laborables, bloques de horario y bloqueos manuales. Los feriados de Guatemala, que vienen de una API, aparecen como días cerrados. |
| Reserva autónoma | El cliente elige día y hora entre los espacios libres calculados en tiempo real. |
| Notificaciones y recordatorios | Notificación al confirmar o reprogramar, y recordatorio programado con WorkManager antes de la cita. Los textos y la anticipación salen de las plantillas que edita el negocio. |
| Historial y estado de citas | El cliente cancela o reprograma. El negocio confirma las citas pendientes y marca como completadas o no presentes las que ya pasaron. También puede ver la agenda de cada barbero. |
| Consulta sin conexión | Toda la agenda vive en Room. Sin internet, el catálogo muestra un aviso y la tarjeta **Tu próxima cita**, y los feriados salen de la caché. |

### Reglas de negocio (`domain/BusinessRules.kt`, con pruebas unitarias)

- Un slot está disponible si cae dentro del horario configurado, no se traslapa con otra cita activa, no tiene un bloqueo manual y no está en el pasado.
- La duración del servicio determina cuántos slots ocupa la cita.
- Un cliente no puede tener dos citas que se traslapen.
- Cancelar con menos de 2 horas de anticipación queda marcado como **cancelación tardía**. En ese margen ya no se puede reprogramar.
- Estados de una cita: pendiente → confirmada → completada / cancelada / no presente. Una cita solo se cierra cuando ya llegó su hora.

---

## Servicios

| Servicio | Tipo | Rol en la app |
|---|---|---|
| **Nager.Date API** (`/api/v3/publicholidays/{año}/GT`) | API REST externa, gratuita y sin API key | Es la llamada a internet de la app. Descarga los feriados de Guatemala para cerrar esos días en la reserva y mostrarlos en Disponibilidad. |
| **Room (SQLite)** | Base de datos local, desarrollada por nosotros | Guarda usuarios, perfil del local, servicios, barberos, citas, horarios, bloqueos, pagos, preferencias, plantillas y la caché de feriados. |
| **Autenticación local** | Desarrollada por nosotros | Las contraseñas y respuestas de seguridad se guardan con hash SHA-256. Las cuentas de clientes las crea el dueño del local. |
| **Notificaciones locales** | Android + WorkManager | Avisos de confirmación y recordatorios. No requieren servidor. |

## Librerías

| Librería | Uso |
|---|---|
| Jetpack Compose + Material 3 | Interfaz de usuario |
| Navigation Compose (type-safe) | Navegación con destinos `@Serializable`. Entre pantallas solo viajan IDs |
| Kotlinx Serialization | Destinos de navegación y JSON de la API |
| Lifecycle ViewModel / Runtime Compose | ViewModels (MVI) y `collectAsStateWithLifecycle` |
| Kotlin Coroutines + Flow | Trabajo asíncrono y estado reactivo |
| Room + KSP | Base de datos local |
| Retrofit + converter-kotlinx-serialization | Cliente HTTP de la API de feriados |
| WorkManager | Recordatorios programados aunque la app esté cerrada |
| JUnit 4 | Pruebas unitarias de las reglas de negocio |

La inyección de dependencias es manual (`di/AppContainer.kt`), sin Hilt ni Koin.

---

## Arquitectura

```
Screen ──Event──▶ ViewModel ──▶ Repository ──▶ Room / Retrofit / WorkManager
  ▲                   │
  └─ StateFlow<State> ┘
```

Cada pantalla tiene su carpeta con **State** (data class), **Event** (sealed interface), **ViewModel** (expone un `StateFlow`) y **Screen**. Dentro de Screen hay una `Route` que conecta el ViewModel con la navegación y una `Screen` stateless con un `@Preview` por estado.

```
app/src/main/java/com/example/turnos/
├── TurnosApp.kt · MainActivity.kt
├── di/             AppContainer · turnosViewModel { }
├── navigation/     Destinations (@Serializable) · TurnosNavHost
├── domain/         BusinessRules · TemplateRenderer
├── notifications/  AppointmentNotifier · AppointmentAlerts · ReminderWorker
├── data/
│   ├── model/        Modelos de dominio
│   ├── local/        Room: TurnosDatabase, entity/, dao/, DatabaseSeeder
│   ├── remote/       Retrofit: HolidaysApi, NetworkModule
│   └── repository/   Auth, Session, Client, Service, Barber, Appointment, Availability, Settings, Holiday
├── util/           Formatos, validaciones, hash de contraseñas, monitor de red
└── ui/
    ├── theme/ · components/ · preview/
    └── screens/    login · forgotpassword · setupaccount · catalog · booking · history
                    business · clients · services · availability · team · payments · notifications
```

### Navegación

```
AuthGraph      Login ─ ForgotPassword ─ SetupAccount(userId)
ClientGraph    Catalog ─ Booking(serviceId, rescheduleAppointmentId) ─ History(asBusiness = false)
BusinessGraph  BusinessHome ─ Clients · Services · Availability · Team · Payments · Notifications
               History(asBusiness = true, barberId)
```

Si hay una sesión guardada, la app abre directamente el inicio del rol correspondiente.

## Pruebas

```bash
./gradlew testDebugUnitTest
```
