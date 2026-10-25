package com.example.turnos

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.turnos.navigation.TurnosNavHost
import com.example.turnos.ui.theme.TurnosTheme

/**
 * Punto de entrada. La app es solo tema claro: los íconos de las barras del sistema siempre son oscuros.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
            navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
        )
        setContent {
            TurnosTheme {
                TurnosNavHost()
            }
        }
    }
}
