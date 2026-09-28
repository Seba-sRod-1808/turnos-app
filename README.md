# App de turnos

App de reservas para negocios locales. Proyecto de Programación de Plataformas Móviles, UVG.
Esta entrega implementa **todas las vistas a nivel visual**: Kotlin + Jetpack Compose + Material 3, con fuente de datos fake y sin backend, de momento se usa de ejemplo una barbería para el local.

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

Quedan para fases posteriores, porque dependen de backend o de APIs externas: el mapa real con Google Maps, el inicio de sesión con Google, el envío real de notificaciones y el modo sin conexión.
## Patrón Route / Screen

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

## Probar la app

Credenciales de prueba: **demo@turnos.com / 123456**. Con “Soy cliente” se entra al catálogo; con “Soy negocio” se entra a Mi Local. Cualquier otra combinación muestra el estado de error.
