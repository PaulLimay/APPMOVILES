package com.example.ligortravel.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ligortravel.data.Reserva
import com.example.ligortravel.ui.misreservas.textoEstado

// Píldora con el estado de una reserva. La comparten Mis reservas y el panel del administrador.
// Naranja = pendiente, verde (tertiary) = pagada, turquesa (secondary) = realizada, rojo (error) =
// cancelada. Recibe el estado visible (ver ReglasCancelacion.estadoVisible), no el guardado.
@Composable
fun EtiquetaEstadoReserva(estado: String) {
    val color = when (estado) {
        Reserva.ESTADO_PAGADA -> MaterialTheme.colorScheme.tertiary
        Reserva.ESTADO_CANCELADA -> MaterialTheme.colorScheme.error
        Reserva.ESTADO_REALIZADA -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(
            text = textoEstado(estado),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
