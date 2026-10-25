package com.example.turnos

import android.app.Application
import com.example.turnos.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application: crea el AppContainer y siembra los datos de demostración la primera vez que se abre la app.
 */

class TurnosApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch { container.seeder.seedIfEmpty() }
    }
}
