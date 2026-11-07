package com.example.ligortravel.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ligortravel.data.RegistroUsuario
import com.example.ligortravel.data.Reserva
import com.example.ligortravel.data.textoMetodoPago
import com.example.ligortravel.ui.components.EtiquetaEstadoReserva
import com.example.ligortravel.ui.misreservas.ReglasCancelacion
import com.example.ligortravel.ui.reserva.aLocalDateUtc
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Filtros de la pestaña Reservas: el estado que muestran (null = todas).
private enum class FiltroReservas(val etiqueta: String, val estado: String?) {
    PENDIENTES("Pendientes", Reserva.ESTADO_PENDIENTE_PAGO),
    PAGADAS("Pagadas", Reserva.ESTADO_PAGADA),
    REALIZADAS("Realizadas", Reserva.ESTADO_REALIZADA),
    CANCELADAS("Canceladas", Reserva.ESTADO_CANCELADA),
    TODAS("Todas", null)
}

@Composable
fun ReservasAdmin(
    reservas: List<Reserva>,
    usuarios: Map<String, RegistroUsuario>,
    procesandoId: String?,
    onRegistrarPago: (Reserva, String) -> Unit,
    onCancelar: (Reserva, String) -> Unit
) {
    var filtro by rememberSaveable { mutableStateOf(FiltroReservas.PENDIENTES) }
    var reservaACobrar by remember { mutableStateOf<Reserva?>(null) }
    var reservaACancelar by remember { mutableStateOf<Reserva?>(null) }
    // "Hoy" decide qué viajes ya se realizaron; se recalcula cada vez que se dibuja la lista.
    val hoy = LocalDate.now()
    val visibles = ReglasCancelacion.filtrarParaAdmin(reservas, filtro.estado, hoy)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroReservas.entries.forEach { opcion ->
                val cantidad = reservas.count {
                    opcion.estado == null || ReglasCancelacion.estadoVisible(it, hoy) == opcion.estado
                }
                FilterChip(
                    selected = filtro == opcion,
                    onClick = { filtro = opcion },
                    label = { Text("${opcion.etiqueta} ($cantidad)") }
                )
            }
        }

        if (visibles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay reservas en este filtro",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibles, key = { it.id }) { reserva ->
                    TarjetaReservaAdmin(
                        reserva = reserva,
                        usuario = usuarios[reserva.usuarioId],
                        hoy = hoy,
                        procesando = procesandoId == reserva.id,
                        habilitado = procesandoId == null,
                        onRegistrarPago = { reservaACobrar = reserva },
                        onCancelar = { reservaACancelar = reserva }
                    )
                }
            }
        }
    }

    reservaACobrar?.let { reserva ->
        DialogoPago(
            reserva = reserva,
            onConfirmar = { metodo ->
                onRegistrarPago(reserva, metodo)
                reservaACobrar = null
            },
            onCerrar = { reservaACobrar = null }
        )
    }
    reservaACancelar?.let { reserva ->
        DialogoCancelarAdmin(
            reserva = reserva,
            onConfirmar = { motivo ->
                onCancelar(reserva, motivo)
                reservaACancelar = null
            },
            onCerrar = { reservaACancelar = null }
        )
    }
}

@Composable
private fun TarjetaReservaAdmin(
    reserva: Reserva,
    usuario: RegistroUsuario?,
    hoy: LocalDate,
    procesando: Boolean,
    habilitado: Boolean,
    onRegistrarPago: () -> Unit,
    onCancelar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = reserva.paqueteTitulo,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            EtiquetaEstadoReserva(ReglasCancelacion.estadoVisible(reserva, hoy))
        }
        Spacer(modifier = Modifier.height(4.dp))
        val fecha = reserva.fechaViaje?.aLocalDateUtc()?.format(formatoFecha) ?: "Sin fecha"
        TextoDato("Cód. ${reserva.id.take(8).uppercase()} · Viaje: $fecha")
        TextoDato(
            "Turista: " + (usuario?.let { "${it.nombre} (${it.email})" } ?: "Usuario ${reserva.usuarioId.take(6)}")
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextoDato(
                if (reserva.personas == 1) "1 persona" else "${reserva.personas} personas",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "S/ ${reserva.total}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            ReglasCancelacion.pagadaEnAgencia(reserva, hoy) -> TextoDato("Viaje realizado · Pagada en la agencia")
            reserva.estado == Reserva.ESTADO_PAGADA -> TextoDato(
                "Pagada con ${textoMetodoPago(reserva.metodoPago)}" +
                    // pagadaEn es un instante (no un día elegido): se muestra en la zona del celular.
                    (reserva.pagadaEn?.let { " el ${it.toDate().toInstant().atZone(ZoneId.systemDefault()).format(formatoFecha)}" } ?: "")
            )
            reserva.estado == Reserva.ESTADO_CANCELADA -> TextoDato(
                (if (reserva.canceladaPor == Reserva.CANCELADA_POR_ADMIN) "Cancelada por la agencia" else "Cancelada por el turista") +
                    (reserva.motivoCancelacion?.let { ": $it" } ?: "")
            )
        }

        val puedeCobrar = ReglasCancelacion.puedeRegistrarPago(reserva, hoy)
        val puedeCancelar = ReglasCancelacion.puedeCancelarAdmin(reserva, hoy)
        if (puedeCobrar || puedeCancelar) {
            Spacer(modifier = Modifier.height(10.dp))
            if (procesando) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (puedeCobrar) {
                        Button(onClick = onRegistrarPago, enabled = habilitado) { Text("Registrar pago") }
                    }
                    if (puedeCancelar) {
                        OutlinedButton(onClick = onCancelar, enabled = habilitado) {
                            Text("Cancelar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextoDato(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        modifier = modifier
    )
}

// El administrador elige cómo pagó el turista. Es un cobro simulado: no se mueve dinero.
@Composable
private fun DialogoPago(reserva: Reserva, onConfirmar: (String) -> Unit, onCerrar: () -> Unit) {
    var metodo by remember { mutableStateOf(Reserva.METODOS_PAGO.first()) }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Registrar pago de S/ ${reserva.total}") },
        text = {
            Column {
                Text(
                    "Cobro simulado: solo se registra cómo pagó el turista.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Reserva.METODOS_PAGO.forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { metodo = opcion },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = metodo == opcion, onClick = { metodo = opcion })
                        Text(textoMetodoPago(opcion))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(metodo) }) { Text("Registrar pago") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Volver") }
        }
    )
}

@Composable
private fun DialogoCancelarAdmin(reserva: Reserva, onConfirmar: (String) -> Unit, onCerrar: () -> Unit) {
    var motivo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("¿Cancelar esta reserva?") },
        text = {
            Column {
                Text(
                    "${reserva.paqueteTitulo}: se liberarán ${reserva.personas} cupos. " +
                        (if (reserva.estado == Reserva.ESTADO_PAGADA) "Ya está pagada: gestiona el reembolso con el turista. " else "") +
                        "No se puede deshacer."
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it.take(MAX_MOTIVO) },
                    label = { Text("Motivo (opcional)") },
                    supportingText = { Text("El turista lo verá en Mis reservas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(motivo) }) {
                Text("Cancelar reserva", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Volver") }
        }
    )
}

// Mismo límite que firestore.rules.
private const val MAX_MOTIVO = 300
