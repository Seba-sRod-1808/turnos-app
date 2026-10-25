package com.example.turnos

import android.app.Application
import com.example.turnos.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TurnosApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Datos de demostración la primera vez que se abre la app.
        appScope.launch { container.seeder.seedIfEmpty() }
    }
}
