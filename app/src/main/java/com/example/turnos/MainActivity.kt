package com.example.turnos

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.example.turnos.navigation.TurnosNavHost
import com.example.turnos.navigation.startDestinationFor
import com.example.turnos.ui.components.SplashContent
import com.example.turnos.ui.theme.TurnosTheme

/**
 * Punto de entrada. La app es solo tema claro: los íconos de las barras del sistema siempre son oscuros.
 * Mientras se preparan los datos y la sesión guardada se muestra el splash; luego se abre
 * directamente el inicio del rol con sesión activa o el login.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
            navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
        )
        val app = application as TurnosApp
        setContent {
            TurnosTheme {
                val startDestination by produceState<Any?>(initialValue = null) {
                    value = startDestinationFor(app.startup.await())
                }
                val destination = startDestination
                if (destination == null) SplashContent() else TurnosNavHost(startDestination = destination)
            }
        }
    }
}
