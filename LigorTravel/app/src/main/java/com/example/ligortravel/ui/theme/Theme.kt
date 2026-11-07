package com.example.ligortravel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Tema global de la app: se aplica una sola vez en MainActivity, y todas las pantallas leen
// sus colores con MaterialTheme.colorScheme. La app es siempre oscura (boceto 1c), así que no
// se usa isSystemInDarkTheme() ni dynamic color (Material You tomaría los colores del fondo
// de pantalla del teléfono en vez de la paleta de LigorTravel).
@Composable
fun LigorTravelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LigorDarkColorScheme,
        typography = Typography,
        content = content
    )
}
