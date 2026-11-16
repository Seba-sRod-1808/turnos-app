package com.example.turnos

import android.app.Application
import com.example.turnos.data.model.User
import com.example.turnos.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

/**
 * Application: crea el AppContainer, siembra los datos de demostración y restaura la sesión.
 * [startup] termina cuando ambas cosas están listas; la UI lo espera antes de mostrar el login
 * para que las cuentas de prueba existan desde el primer intento.
 */

class TurnosApp : Application() {

    lateinit var container: AppContainer
        private set

    lateinit var startup: Deferred<User?>
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.appointmentNotifier.createChannel()
        startup = appScope.async {
            container.seeder.seedIfEmpty()
            container.sessionRepository.restore()
        }
    }
}
