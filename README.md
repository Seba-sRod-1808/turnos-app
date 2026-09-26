# App de Turnos/citas

App de reservas para negocios locales como barberias. Proyecto de Programación de Plataformas Móviles, UVG. 

Tecnologías: Kotlin + Jetpack Compose + Material 3.

## Estructura

## Flujos

- **Cliente:** Login  → Catálogo → Reservar → elegir día/horario → Confirmar → Ver cita en agenda → Historial (cancelar / reprogramar).
- **Negocio:** Login → Mi Local → Servicios, Horarios, Equipo, Historial, Pagos, Notificaciones.

Cada pantalla tiene un preview para verla en Android Studio sin correr la app.

## Siguientes pasos

- Reemplazar el mock de la data por repositorios + ViewModels con state flow.
- Room para el modo de consulta sin conexión.
- Coil para fotos reales, Maps Compose para el mapa, Credential Manager para Google Sign-In.
- FCM para notificaciones y recordatorios.
