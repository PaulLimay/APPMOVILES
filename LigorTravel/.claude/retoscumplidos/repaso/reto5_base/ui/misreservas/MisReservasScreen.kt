package com.example.ligortravel.ui.misreservas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.ui.components.BotonVolver
import com.example.ligortravel.ui.components.TabPill
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localePeru = Locale.forLanguageTag("es-PE")
private val formatoMes = DateTimeFormatter.ofPattern("MMM", localePeru)
private val formatoFechaLarga = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM, yyyy", localePeru)

private enum class PestanaReservas { PROXIMOS, REALIZADOS }

@Composable
fun MisReservasScreen(
    onVolver: () -> Unit,
    onVerPaquetes: () -> Unit,
    viewModel: MisReservasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // La pestaña elegida es estado de la UI (no del negocio), igual que el tab de AuthScreen.
    // rememberSaveable la conserva al rotar la pantalla o al volver desde otra.
    var pestana by rememberSaveable { mutableStateOf(PestanaReservas.PROXIMOS) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Encabezado(onVolver)

            SelectorPestanas(
                seleccionada = pestana,
                cantidadProximos = uiState.proximas.size,
                cantidadRealizados = uiState.realizadas.size,
                onSeleccionar = { pestana = it }
            )

            when {
                uiState.cargando -> MensajeCentrado {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                uiState.error != null -> MensajeCentrado {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            viewModel.reintentarReservas()
                        })
                    {
                        Text("Reintentar")
                    }
                }

                else -> {
                    val reservas = if (pestana == PestanaReservas.PROXIMOS) uiState.proximas
                    else uiState.realizadas
                    if (reservas.isEmpty()) {
                        ListaVacia(pestana, onVerPaquetes)
                    } else {
                        ListaReservas(
                            reservas = reservas,
                            esProximo = pestana == PestanaReservas.PROXIMOS,
                            hoy = uiState.hoy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonVolver(onClick = onVolver)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Mis reservas",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SelectorPestanas(
    seleccionada: PestanaReservas,
    cantidadProximos: Int,
    cantidadRealizados: Int,
    onSeleccionar: (PestanaReservas) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .padding(4.dp)
    ) {
        TabPill(
            texto = "Próximos ($cantidadProximos)",
            seleccionada = seleccionada == PestanaReservas.PROXIMOS,
            modifier = Modifier.weight(1f)
        ) { onSeleccionar(PestanaReservas.PROXIMOS) }
        TabPill(
            texto = "Realizados ($cantidadRealizados)",
            seleccionada = seleccionada == PestanaReservas.REALIZADOS,
            modifier = Modifier.weight(1f)
        ) { onSeleccionar(PestanaReservas.REALIZADOS) }
    }
}

@Composable
private fun ListaReservas(reservas: List<ReservaConFecha>, esProximo: Boolean, hoy: LocalDate) {
    LazyColumn(
        modifier = Modifier.navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // key: igual que en PaquetesScreen, para que Compose no confunda tarjetas si la lista
        // cambia en tiempo real (por ejemplo, al llegar una reserva nueva).
        items(reservas, key = { it.reserva.id }) { item ->
            TarjetaReserva(item, esProximo, hoy)
        }
    }
}

@Composable
private fun TarjetaReserva(item: ReservaConFecha, esProximo: Boolean, hoy: LocalDate) {
    val reserva = item.reserva

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            // Los viajes realizados se ven un poco apagados: ya son historia.
            .alpha(if (esProximo) 1f else 0.75f)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InsigniaFecha(item.fecha, resaltada = esProximo)
        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reserva.paqueteTitulo,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = item.fecha.format(formatoFechaLarga).replaceFirstChar { it.uppercase() },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (reserva.personas == 1) "1 persona" else "${reserva.personas} personas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "S/ ${reserva.total}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                EtiquetaEstado(textoEstado(reserva.estado))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (esProximo) textoCuentaRegresiva(item.fecha, hoy)
                    else "Cód. ${reserva.id.take(8).uppercase()}",
                    color = if (esProximo) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = if (esProximo) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// Bloque tipo "hoja de calendario" con el día, el mes y el año.
@Composable
private fun InsigniaFecha(fecha: LocalDate, resaltada: Boolean) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (resaltada) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.background
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val color = if (resaltada) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant
        Text(
            // trimEnd('.') porque en español el mes abreviado viene como "oct.".
            text = fecha.format(formatoMes).trimEnd('.').uppercase(),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = fecha.dayOfMonth.toString(),
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = fecha.year.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun EtiquetaEstado(texto: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = texto,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ListaVacia(pestana: PestanaReservas, onVerPaquetes: () -> Unit) {
    val (icono: ImageVector, titulo, detalle) = when (pestana) {
        PestanaReservas.PROXIMOS -> Triple(
            Icons.Filled.CardTravel,
            "No tienes viajes próximos",
            "Reserva un paquete y aparecerá aquí."
        )
        PestanaReservas.REALIZADOS -> Triple(
            Icons.Filled.History,
            "Aún no tienes viajes realizados",
            "Cuando pase la fecha de tus viajes, los verás aquí."
        )
    }
    MensajeCentrado {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = titulo,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detalle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        if (pestana == PestanaReservas.PROXIMOS) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onVerPaquetes) {
                Text("Ver paquetes")
            }
        }
    }
}

@Composable
private fun MensajeCentrado(contenido: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            contenido()
        }
    }
}
