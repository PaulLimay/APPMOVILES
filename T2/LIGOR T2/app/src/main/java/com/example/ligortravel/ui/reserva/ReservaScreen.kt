package com.example.ligortravel.ui.reserva

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.ui.components.BotonVolver
import com.example.ligortravel.ui.components.PantallaEstado
import com.example.ligortravel.ui.components.Seccion
import com.example.ligortravel.ui.home.imagenDeExperiencia
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Formato largo en español, ej. "lunes, 20 de octubre de 2026".
private val formatoFecha = DateTimeFormatter.ofPattern(
    "EEEE, d 'de' MMMM 'de' yyyy",
    Locale.forLanguageTag("es-PE")
)

@Composable
fun ReservaScreen(
    paqueteId: String,
    onVolver: () -> Unit,
    onIrAlInicio: () -> Unit,
    onVerMisReservas: () -> Unit,
    viewModel: ReservaViewModel = viewModel { ReservaViewModel(paqueteId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val paquete = uiState.paquete

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        when {
            // Va primero: una vez registrada, la confirmación se queda aunque el paquete cambie.
            uiState.reservaExitosa && paquete != null -> ConfirmacionReserva(
                uiState = uiState,
                paquete = paquete,
                onIrAlInicio = onIrAlInicio,
                onVerMisReservas = onVerMisReservas
            )
            paquete != null -> FormularioReserva(
                uiState = uiState,
                paquete = paquete,
                onVolver = onVolver,
                onFechaChange = viewModel::onFechaChange,
                onPersonasChange = viewModel::onPersonasChange,
                onConfirmar = viewModel::confirmarReserva
            )
            uiState.cargandoPaquete -> PantallaEstado(onVolver) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            else -> PantallaEstado(onVolver) {
                Text(
                    text = uiState.errorPaquete.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FormularioReserva(
    uiState: ReservaUiState,
    paquete: Paquete,
    onVolver: () -> Unit,
    onFechaChange: (LocalDate) -> Unit,
    onPersonasChange: (Int) -> Unit,
    onConfirmar: () -> Unit
) {
    // Si el diálogo del calendario está abierto o no es estado de la UI, no del negocio: por eso
    // vive aquí con rememberSaveable (sobrevive a la rotación) y no en el ViewModel.
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Encabezado(onVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            ResumenPaquete(paquete)

            Seccion("Fecha del viaje") {
                SelectorFecha(
                    fecha = uiState.fecha,
                    habilitado = !uiState.cargando,
                    onClick = { mostrarCalendario = true }
                )
            }

            Seccion("Número de personas") {
                SelectorPersonas(
                    personas = uiState.personas,
                    habilitado = !uiState.cargando,
                    onPersonasChange = onPersonasChange
                )
            }

            Seccion("Resumen") {
                ResumenPrecio(
                    precioUnitario = paquete.precio,
                    personas = uiState.personas,
                    total = uiState.total
                )
                Spacer(modifier = Modifier.height(12.dp))
                AvisoPagoPendiente()
            }

            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }

        BarraConfirmar(
            total = uiState.total,
            cargando = uiState.cargando,
            onConfirmar = onConfirmar
        )
    }

    if (mostrarCalendario) {
        DialogoCalendario(
            fechaInicial = uiState.fecha,
            onFechaElegida = { fecha ->
                onFechaChange(fecha)
                mostrarCalendario = false
            },
            onCerrar = { mostrarCalendario = false }
        )
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
            text = "Reservar paquete",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ResumenPaquete(paquete: Paquete) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            val imagenRes = imagenDeExperiencia(paquete.imagen)
            if (imagenRes != null) {
                Image(
                    painter = painterResource(imagenRes),
                    contentDescription = paquete.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = paquete.titulo,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = paquete.destino,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = paquete.duracion,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Campo que parece un input; al tocarlo abre el calendario.
@Composable
private fun SelectorFecha(fecha: LocalDate?, habilitado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            // replaceFirstChar pone en mayúscula la primera letra ("lunes" -> "Lunes").
            text = fecha?.format(formatoFecha)?.replaceFirstChar { it.uppercase() } ?: "Elegir fecha",
            color = if (fecha != null) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// DatePicker de Material 3 dentro de un diálogo. selectableDates deshabilita en el propio
// calendario los días fuera del rango permitido: el usuario ni siquiera puede tocarlos.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoCalendario(
    fechaInicial: LocalDate?,
    onFechaElegida: (LocalDate) -> Unit,
    onCerrar: () -> Unit
) {
    val hoy = remember { LocalDate.now() }
    val estado = rememberDatePickerState(
        // El DatePicker trabaja en milisegundos UTC (ver aMillisUtc en ReglasReserva.kt).
        initialSelectedDateMillis = fechaInicial?.aMillisUtc(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                ReglasReserva.esFechaValida(millisUtcALocalDate(utcTimeMillis), hoy)

            override fun isSelectableYear(year: Int): Boolean =
                year in hoy.year..ReglasReserva.fechaMaxima(hoy).year
        }
    )

    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(
                onClick = { estado.selectedDateMillis?.let { onFechaElegida(millisUtcALocalDate(it)) } },
                enabled = estado.selectedDateMillis != null
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = estado)
    }
}

// "Stepper": botones − y + que se deshabilitan al llegar a los límites.
@Composable
private fun SelectorPersonas(personas: Int, habilitado: Boolean, onPersonasChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (personas == 1) "1 persona" else "$personas personas",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Máximo ${ReglasReserva.PERSONAS_MAX} por reserva",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        BotonCantidad(
            icono = { Icon(Icons.Filled.Remove, contentDescription = "Quitar una persona") },
            habilitado = habilitado && personas > ReglasReserva.PERSONAS_MIN,
            onClick = { onPersonasChange(personas - 1) }
        )
        Text(
            text = personas.toString(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
        )
        BotonCantidad(
            icono = { Icon(Icons.Filled.Add, contentDescription = "Agregar una persona") },
            habilitado = habilitado && personas < ReglasReserva.PERSONAS_MAX,
            onClick = { onPersonasChange(personas + 1) }
        )
    }
}

@Composable
private fun BotonCantidad(icono: @Composable () -> Unit, habilitado: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.background, CircleShape)
    ) {
        icono()
    }
}

@Composable
private fun ResumenPrecio(precioUnitario: Int, personas: Int, total: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilaResumen("Precio por persona", "S/ $precioUnitario")
        FilaResumen("Personas", "× $personas")
        HorizontalDivider(color = MaterialTheme.colorScheme.background)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "S/ $total",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(text = valor, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
    }
}

@Composable
private fun AvisoPagoPendiente() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Por ahora no se cobra nada en la app: tu reserva quedará registrada como pendiente de pago.",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun BarraConfirmar(total: Int, cargando: Boolean, onConfirmar: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = "S/ $total",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Mientras se guarda, el botón se deshabilita y muestra un indicador de carga: así el
            // usuario sabe que algo está pasando y no puede enviar la reserva dos veces.
            Button(onClick = onConfirmar, enabled = !cargando) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reservando...")
                } else {
                    Text("Confirmar reserva")
                }
            }
        }
    }
}

@Composable
private fun ConfirmacionReserva(
    uiState: ReservaUiState,
    paquete: Paquete,
    onIrAlInicio: () -> Unit,
    onVerMisReservas: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "¡Reserva registrada!",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Guardamos tu reserva. Te contactaremos para coordinar el pago.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Los primeros 8 caracteres del id de Firestore bastan para que el usuario lo dicte.
            FilaResumen("Código", uiState.codigoReserva.orEmpty().take(8).uppercase())
            FilaResumen("Paquete", paquete.titulo)
            uiState.fecha?.let { FilaResumen("Fecha", it.format(formatoFecha)) }
            FilaResumen("Personas", uiState.personas.toString())
            FilaResumen("Total", "S/ ${uiState.total}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Pendiente de pago",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onVerMisReservas, modifier = Modifier.fillMaxWidth()) {
            Text("Ver mis reservas")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onIrAlInicio, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al inicio")
        }
    }
}
