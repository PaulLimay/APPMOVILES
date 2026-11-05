package com.example.ligortravel.ui.paquetes

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.ui.components.BotonVolver
import com.example.ligortravel.ui.components.FichaDato
import com.example.ligortravel.ui.components.PantallaEstado
import com.example.ligortravel.ui.components.Seccion
import com.example.ligortravel.ui.home.imagenDeExperiencia

@Composable
fun DetallePaqueteScreen(
    paqueteId: String,
    onVolver: () -> Unit,
    onAbrirExperiencia: (experienciaId: String) -> Unit,
    onReservar: (paqueteId: String) -> Unit,
    // Igual que en DetalleScreen: el ViewModel se construye con el id solo la primera vez.
    viewModel: DetallePaqueteViewModel = viewModel { DetallePaqueteViewModel(paqueteId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val resumen = uiState.resumen

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        when {
            // Si ya hay datos se muestran aunque luego falle el listener (ej. se cortó internet).
            resumen != null -> ContenidoDetallePaquete(
                resumen = resumen,
                onVolver = onVolver,
                onAbrirExperiencia = onAbrirExperiencia,
                onReservar = { onReservar(resumen.paquete.id) }
            )
            uiState.cargando -> PantallaEstado(onVolver) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            else -> PantallaEstado(onVolver) {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ContenidoDetallePaquete(
    resumen: PaqueteResumen,
    onVolver: () -> Unit,
    onAbrirExperiencia: (String) -> Unit,
    onReservar: () -> Unit
) {
    val paquete = resumen.paquete

    // Dos partes: arriba el contenido con scroll (weight(1f) ocupa todo el alto que sobra) y
    // abajo la barra de reserva, que queda fija y siempre a la vista.
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Portada(paquete, onVolver)

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(20.dp))
                EncabezadoPaquete(paquete)

                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FichaDato(
                        icono = Icons.Filled.Schedule,
                        etiqueta = "Duración",
                        valor = paquete.duracion,
                        modifier = Modifier.weight(1f)
                    )
                    FichaDato(
                        icono = Icons.Filled.Hiking,
                        etiqueta = "Actividades",
                        valor = resumen.experiencias.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (paquete.descripcion.isNotBlank()) {
                    Seccion("Descripción") {
                        Text(
                            text = paquete.descripcion,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                    }
                }

                Seccion("Actividades") {
                    ListaActividades(resumen.experiencias, onAbrirExperiencia)
                }

                if (paquete.serviciosIncluidos.isNotEmpty()) {
                    Seccion("Servicios incluidos") {
                        ListaServicios(paquete.serviciosIncluidos)
                    }
                }

                Seccion("Precio") {
                    DesglosePrecio(resumen)
                }
            }
        }

        BarraReserva(precio = paquete.precio, onReservar = onReservar)
    }
}

@Composable
private fun Portada(paquete: Paquete, onVolver: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(MaterialTheme.colorScheme.surface)
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
            )
        }
        BotonVolver(
            onClick = onVolver,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp)
        )
    }
}

@Composable
private fun EncabezadoPaquete(paquete: Paquete) {
    Surface(
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = "Paquete turístico",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = paquete.titulo,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = paquete.destino,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

// Las experiencias del paquete en orden (1, 2, 3...); al tocar una se abre su detalle.
@Composable
private fun ListaActividades(experiencias: List<Experiencia>, onAbrirExperiencia: (String) -> Unit) {
    if (experiencias.isEmpty()) {
        Text(
            text = "Las actividades de este paquete no están disponibles por ahora",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        experiencias.forEachIndexed { indice, experiencia ->
            FilaActividad(
                numero = indice + 1,
                experiencia = experiencia,
                onClick = { onAbrirExperiencia(experiencia.id) }
            )
        }
    }
}

@Composable
private fun FilaActividad(numero: Int, experiencia: Experiencia, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = experiencia.titulo,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            // Solo los datos que existan, separados por " · ".
            val detalle = listOf(experiencia.duracion, experiencia.ubicacion)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            if (detalle.isNotEmpty()) {
                Text(
                    text = detalle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Ver ${experiencia.titulo}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ListaServicios(servicios: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        servicios.forEach { servicio ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = servicio,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Compara lo que costarían las actividades por separado con el precio del paquete.
@Composable
private fun DesglosePrecio(resumen: PaqueteResumen) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (resumen.precioPorSeparado > 0) {
            FilaPrecio(
                etiqueta = "Actividades por separado",
                valor = "S/ ${resumen.precioPorSeparado}",
                // Tachado solo si de verdad el paquete sale más barato.
                tachado = resumen.ahorro > 0
            )
        }
        FilaPrecio(
            etiqueta = "Precio del paquete (por persona)",
            valor = "S/ ${resumen.paquete.precio}",
            color = MaterialTheme.colorScheme.primary,
            negrita = true
        )
        if (resumen.ahorro > 0) {
            HorizontalDivider(color = MaterialTheme.colorScheme.background)
            FilaPrecio(
                etiqueta = "Ahorras",
                valor = "S/ ${resumen.ahorro}",
                color = MaterialTheme.colorScheme.tertiary,
                negrita = true
            )
        }
    }
}

@Composable
private fun FilaPrecio(
    etiqueta: String,
    valor: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    negrita: Boolean = false,
    tachado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Text(
            text = valor,
            color = color,
            fontSize = 15.sp,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            textDecoration = if (tachado) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

// Barra fija inferior: el precio siempre visible y el acceso directo a reservar.
@Composable
private fun BarraReserva(precio: Int, onReservar: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Deja espacio para la barra de gestos/botones del sistema (edge-to-edge).
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "por persona",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = "S/ $precio",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(onClick = onReservar) {
                Icon(imageVector = Icons.Filled.EventAvailable, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reservar")
            }
        }
    }
}
