package com.example.turnos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.example.turnos.data.local.DatabaseSeeder
import com.example.turnos.data.local.TurnosDatabase
import com.example.turnos.data.local.entity.ServiceEntity
import com.example.turnos.data.model.Appointment
import com.example.turnos.data.model.AppointmentStatus
import com.example.turnos.data.model.SlotState
import com.example.turnos.data.model.TimeBlock
import com.example.turnos.data.model.TimeSlot
import com.example.turnos.data.model.User
import com.example.turnos.data.model.UserRole
import com.example.turnos.data.remote.HolidayDto
import com.example.turnos.data.remote.HolidaysApi
import com.example.turnos.data.repository.AppointmentRepository
import com.example.turnos.data.repository.AvailabilityRepository
import com.example.turnos.data.repository.BarberRepository
import com.example.turnos.data.repository.HolidayRepository
import com.example.turnos.data.repository.ServiceRepository
import com.example.turnos.data.repository.SessionRepository
import com.example.turnos.data.repository.SettingsRepository
import com.example.turnos.domain.BusinessRules
import com.example.turnos.notifications.AppointmentAlerts
import com.example.turnos.notifications.AppointmentNotifier
import com.example.turnos.ui.components.Avatar
import com.example.turnos.ui.screens.booking.BookingEvent
import com.example.turnos.ui.screens.booking.BookingState
import com.example.turnos.ui.screens.booking.BookingViewModel
import com.example.turnos.ui.screens.booking.DayOption
import com.example.turnos.ui.screens.login.LoginEvent
import com.example.turnos.ui.screens.login.LoginScreen
import com.example.turnos.ui.screens.login.LoginState
import com.example.turnos.ui.screens.notifications.TemplateForm
import com.example.turnos.ui.screens.services.ServiceForm
import com.example.turnos.ui.theme.TurnosTheme
import com.example.turnos.util.Validators
import com.example.turnos.util.formatTime
import com.example.turnos.util.quetzales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TurnosAdversarialTestSuite {

    @get:Rule
    val composeRule = createComposeRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var db: TurnosDatabase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = RuntimeEnvironment.getApplication()
        val executor = testDispatcher.asExecutor()
        db = Room.inMemoryDatabaseBuilder(context, TurnosDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(executor)
            .setTransactionExecutor(executor)
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `1_Validators_isValidPhone_rejects_strings_without_digits`() {
        assertFalse(
            "Un teléfono formado únicamente por guiones '--------' no debe ser válido",
            Validators.isValidPhone("--------"),
        )
        assertFalse(
            "Un teléfono formado únicamente por signos y espacios '+ - + - +' no debe ser válido",
            Validators.isValidPhone("+ - + - +"),
        )
    }

    @Test
    fun `2_Validators_isValidPassword_rejects_whitespace_only_passwords`() {
        assertFalse(
            "Una contraseña compuesta solo por espacios en blanco ('      ') debe ser rechazada",
            Validators.isValidPassword("      "),
        )
    }

    @Test
    fun `3_Formatters_quetzales_formats_negative_amounts_with_minus_before_currency`() {
        assertEquals(
            "Los montos negativos deben formatearse como -Q1,200 y no Q-1,200",
            "-Q1,200",
            quetzales(-1200),
        )
    }

    @Test
    fun `4_Formatters_formatTime_handles_out_of_range_and_midnight_wrap_safely`() {
        val midnightWrap = formatTime(1440)
        assertEquals("1440 minutos (24:00) debe envolver a 12:00 AM, no 12:00 PM", "12:00 AM", midnightWrap)

        val negativeMinutes = formatTime(-15)
        assertFalse(
            "Minutos negativos no deben producir minutos con signo negativo en el texto ($negativeMinutes)",
            negativeMinutes.contains("-"),
        )
    }

    @Test
    fun `5_Appointment_startDateTime_does_not_crash_on_boundary_startMinutes`() {
        val appointment = Appointment(
            id = 1,
            serviceId = 1,
            serviceName = "Corte",
            barberId = 1,
            barberName = "Alex",
            clientId = 2,
            clientName = "Ana",
            date = LocalDate.of(2026, 9, 14),
            startMinutes = 1440,
            durationMin = 30,
            price = 85,
            status = AppointmentStatus.CONFIRMED,
        )
        val dt = appointment.startDateTime
        assertEquals(LocalDate.of(2026, 9, 14), dt.toLocalDate())
    }

    @Test
    fun `6_BusinessRules_computeSlots_deduplicates_overlapping_time_blocks`() {
        val date = LocalDate.of(2026, 9, 14)
        val now = LocalDateTime.of(2026, 9, 10, 8, 0)
        val overlappingBlocks = listOf(
            TimeBlock(id = 1, startMinutes = 9 * 60, endMinutes = 12 * 60),
            TimeBlock(id = 2, startMinutes = 10 * 60, endMinutes = 13 * 60),
        )
        val slots = BusinessRules.computeSlots(
            date = date,
            durationMin = 30,
            blocks = overlappingBlocks,
            busy = emptyList(),
            blockedStarts = emptySet(),
            now = now,
        )
        val uniqueStarts = slots.map { it.startMinutes }.toSet()
        assertEquals(
            "Bloques horarios traslapados no deben producir TimeSlots duplicados con el mismo startMinutes",
            uniqueStarts.size,
            slots.size,
        )
    }

    @Test
    fun `7_BusinessRules_computeSlots_rejects_zero_or_negative_duration`() {
        val date = LocalDate.of(2026, 9, 14)
        val now = LocalDateTime.of(2026, 9, 10, 8, 0)
        val blocks = listOf(TimeBlock(1, 9 * 60, 12 * 60))
        val slotsZero = BusinessRules.computeSlots(date, durationMin = 0, blocks = blocks, busy = emptyList(), blockedStarts = emptySet(), now = now)
        val slotsNegative = BusinessRules.computeSlots(date, durationMin = -30, blocks = blocks, busy = emptyList(), blockedStarts = emptySet(), now = now)

        assertTrue("Una duración <= 0 no debe generar horarios disponibles", slotsZero.isEmpty() && slotsNegative.isEmpty())
    }

    @Test
    fun `8_ServiceForm_isValid_rejects_zero_and_negative_prices`() {
        val zeroPriceForm = ServiceForm(name = "Corte", description = "Desc", duration = "30", price = "0")
        val negativePriceForm = ServiceForm(name = "Corte", description = "Desc", duration = "30", price = "-150")

        assertFalse("Un servicio con precio 0 no debe ser válido", zeroPriceForm.isValid)
        assertFalse("Un servicio con precio negativo (-150) no debe ser válido", negativePriceForm.isValid)
    }

    @Test
    fun `9_TemplateForm_isValid_rejects_malformed_or_unclosed_placeholders`() {
        val malformed = TemplateForm(
            id = 1,
            title = "Recordatorio",
            channel = "Notificación local",
            timing = "2 h antes",
            body = "Hola {cliente, tu cita es a las {hora}",
        )
        assertFalse("Una plantilla con llaves sin cerrar '{cliente' debe ser inválida", malformed.isValid)
    }

    @Test
    fun `10_BookingState_canConfirm_requires_open_selectedDay_and_available_slot`() {
        val date = LocalDate.of(2026, 9, 15)
        val closedHolidayDay = DayOption(date = date, label = "Mar 15", isWorkDay = true, holidayName = "Día de la Independencia")
        val stateOnHoliday = BookingState(
            isLoading = false,
            days = listOf(closedHolidayDay),
            selectedDate = date,
            selectedSlot = 9 * 60,
            slots = listOf(TimeSlot(9 * 60, SlotState.OCCUPIED)),
        )
        assertFalse(
            "canConfirm debe ser false si el día seleccionado está cerrado o el slot no está AVAILABLE",
            stateOnHoliday.canConfirm,
        )
    }

    @Test
    fun `11_BookingViewModel_does_not_crash_when_workDays_is_empty_and_rejects_inactive_service`() = runTest(testDispatcher) {
        val id = db.serviceDao().upsert(
            ServiceEntity(
                name = "Servicio Pausado",
                description = "No disponible",
                durationMin = 30,
                price = 100,
                active = false,
            )
        )
        val vm = createBookingViewModel(serviceId = id)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(
            "Un servicio inactivo/pausado no debe poder cargarse para reservar",
            null,
            state.service,
        )
        assertTrue(
            "Debe mostrarse un mensaje de error cuando el servicio está inactivo",
            state.errorMessage != null,
        )
    }

    @Test
    fun `12_BookingViewModel_confirm_prevents_duplicate_appointments_on_double_click`() = runTest(testDispatcher) {
        DatabaseSeeder(db).seedIfEmpty()
        val services = db.serviceDao().observeActive().first()
        val activeService = services.first()

        val vm = createBookingViewModel(serviceId = activeService.id)
        advanceUntilIdle()

        val beforeCount = db.appointmentDao().observeAll().first().size
        vm.state.value.days.first { day ->
            vm.onEvent(BookingEvent.OnSelectDay(day.date))
            advanceUntilIdle()
            vm.state.value.slots.any { it.state == SlotState.AVAILABLE }
        }
        val firstAvailableSlot = vm.state.value.slots.first { it.state == SlotState.AVAILABLE }.startMinutes
        vm.onEvent(BookingEvent.OnSelectSlot(firstAvailableSlot))

        vm.onEvent(BookingEvent.OnConfirmClick)
        vm.onEvent(BookingEvent.OnConfirmClick)
        advanceUntilIdle()

        val afterCount = db.appointmentDao().observeAll().first().size
        assertEquals(
            "Un doble clic rápido en CONFIRMAR RESERVA solo debe crear 1 cita nueva en base de datos",
            beforeCount + 1,
            afterCount,
        )
    }

    @Test
    fun `13_Avatar_renders_fallback_on_blank_name_and_handles_surrogate_pair_emojis`() {
        composeRule.setContent {
            TurnosTheme {
                Avatar("   ")
                Avatar("\uD83E\uDDD4 Alex")
            }
        }
        composeRule.onNodeWithText("?").assertIsDisplayed()
        composeRule.onNodeWithText("\uD83E\uDDD4A").assertIsDisplayed()
    }

    @Test
    fun `14_LoginScreen_PasswordField_ImeAction_Done_triggers_OnLoginClick`() {
        val events = mutableListOf<LoginEvent>()
        composeRule.setContent {
            TurnosTheme {
                LoginScreen(
                    state = LoginState(username = "cliente", password = "secret"),
                    onEvent = { events.add(it) },
                )
            }
        }
        composeRule.onNodeWithText("secret").performImeAction()
        assertTrue(
            "Presionar Done en el teclado del campo de contraseña debe disparar LoginEvent.OnLoginClick",
            events.contains(LoginEvent.OnLoginClick),
        )
    }

    private fun createBookingViewModel(serviceId: Long): BookingViewModel {
        val context = RuntimeEnvironment.getApplication()
        val session = SessionRepository(context, db.userDao()).apply {
            start(User(id = 10L, name = "Ana López", username = "cliente", phone = "55551234", role = UserRole.CLIENT, mustChangePassword = false))
        }
        val settings = SettingsRepository(db.settingsDao())
        val notifier = AppointmentNotifier(context, settings)
        val alerts = AppointmentAlerts(context, notifier, settings)
        val holidaysApi = object : HolidaysApi {
            override suspend fun getPublicHolidays(year: Int, countryCode: String): Response<List<HolidayDto>> =
                Response.success(emptyList())
        }

        return BookingViewModel(
            savedStateHandle = SavedStateHandle(mapOf("serviceId" to serviceId, "rescheduleAppointmentId" to -1L)),
            serviceRepository = ServiceRepository(db.serviceDao()),
            appointmentRepository = AppointmentRepository(db.appointmentDao()),
            availabilityRepository = AvailabilityRepository(db.availabilityDao()),
            barberRepository = BarberRepository(db.barberDao(), db.appointmentDao()),
            holidayRepository = HolidayRepository(holidaysApi, db.holidayDao()),
            session = session,
            settings = settings,
            alerts = alerts,
        )
    }
}
