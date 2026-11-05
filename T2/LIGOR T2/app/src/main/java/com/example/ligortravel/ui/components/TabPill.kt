package com.example.ligortravel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Pestaña con forma de píldora: rellena de naranja si está seleccionada. Se usa dentro de un Row
// con fondo surface (ej. Ingresar/Crear cuenta en AuthScreen, Próximos/Realizados en MisReservas).
@Composable
fun TabPill(texto: String, seleccionada: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val fondo = if (seleccionada) MaterialTheme.colorScheme.primary else Color.Transparent
    val contenido = if (seleccionada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .background(fondo, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = texto, color = contenido, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
