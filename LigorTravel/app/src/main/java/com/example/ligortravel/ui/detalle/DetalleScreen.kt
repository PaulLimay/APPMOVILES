package com.example.ligortravel.ui.detalle

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.ui.components.BotonVolver
import com.example.ligortravel.ui.components.FichaDato
import com.example.ligortravel.ui.components.PantallaEstado
import com.example.ligortravel.ui.components.Seccion
import com.example.ligortravel.ui.components.ImagenCatalogo

@Composable
fun DetalleScreen(
    experienciaId: String,
    onVolver: () -> Unit,
    // viewModel { ... } le dice a Compose CÓMO construir el ViewModel (con el id), pero solo lo
    // construye la primera vez: al rotar la pantalla se reutiliza el mismo ViewModel.
    viewModel: DetalleViewModel = viewModel { DetalleViewModel(experienciaId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val experiencia = uiState.experiencia

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        when {
            // Si ya hay datos se muestran aunque luego falle el listener (ej. se cortó internet).
            experiencia != null -> ContenidoDetalle(
                experiencia = experiencia,
                fotos = uiState.fotos,
                fotoSeleccionada = uiState.fotoSeleccionada,
                onSeleccionarFoto = viewModel::onSeleccionarFoto,
                onVolver = onVolver,
                resenas = { SeccionResenas(experiencia, uiState, viewModel) }
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
private fun ContenidoDetalle(
    experiencia: Experiencia,
    fotos: List<String>,
    fotoSeleccionada: Int,
    onSeleccionarFoto: (Int) -> Unit,
    onVolver: () -> Unit,
    // La sección de reseñas llega como composable para no pasar aquí todo su estado.
    resenas: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
    ) {
        FotoPrincipal(
            nombreImagen = fotos.getOrNull(fotoSeleccionada),
            titulo = experiencia.titulo,
            onVolver = onVolver
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(20.dp))
            EncabezadoDetalle(experiencia)

            Spacer(modifier = Modifier.height(20.dp))
            DatosClave(experiencia)

            if (experiencia.descripcion.isNotBlank()) {
                Seccion("Descripción") {
                    Text(
                        text = experiencia.descripcion,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            // La galería solo tiene sentido si hay más de una foto para elegir.
            if (fotos.size > 1) {
                Seccion("Galería") {
                    Galeria(
                        fotos = fotos,
                        fotoSeleccionada = fotoSeleccionada,
                        onSeleccionarFoto = onSeleccionarFoto
                    )
                }
            }

            if (experiencia.incluye.isNotEmpty()) {
                Seccion("Incluye") {
                    ListaIncluye(experiencia.incluye)
                }
            }

            Seccion("Ubicación") {
                TarjetaUbicacion(experiencia)
            }

            Seccion("Reseñas") {
                resenas()
            }
        }
    }
}

@Composable
private fun FotoPrincipal(nombreImagen: String?, titulo: String, onVolver: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        ImagenCatalogo(
            referencia = nombreImagen,
            contentDescription = titulo,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
            )
        }

        // La foto llega hasta arriba (edge-to-edge); statusBarsPadding baja el botón para que
        // no quede debajo de la hora y la batería.
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
private fun EncabezadoDetalle(experiencia: Experiencia) {
    experiencia.categoria?.let { categoria ->
        Surface(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = categoria,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
    }

    Text(
        text = experiencia.titulo,
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
            text = experiencia.ubicacion,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                // Promedio real de las reseñas, o "Nuevo" si todavía no tiene.
                text = ReglasResenas.textoPromedio(experiencia) +
                    if (experiencia.totalResenas > 0) " (${experiencia.totalResenas})" else "",
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "desde",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = "S/ ${experiencia.precioDesde}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Fila de "fichas" con los datos rápidos del tour (duración, mejor época).
@Composable
private fun DatosClave(experiencia: Experiencia) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (experiencia.duracion.isNotBlank()) {
            FichaDato(
                icono = Icons.Filled.Schedule,
                etiqueta = "Duración",
                valor = experiencia.duracion,
                modifier = Modifier.weight(1f)
            )
        }
        experiencia.mejorEpoca?.takeIf { it.isNotBlank() }?.let { epoca ->
            FichaDato(
                icono = Icons.Filled.CalendarMonth,
                etiqueta = "Mejor época",
                valor = epoca,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Miniaturas en fila horizontal; al tocar una, pasa a verse en grande arriba.
@Composable
private fun Galeria(fotos: List<String>, fotoSeleccionada: Int, onSeleccionarFoto: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(fotos) { indice, nombre ->
            val seleccionada = indice == fotoSeleccionada
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 2.dp,
                        color = if (seleccionada) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSeleccionarFoto(indice) },
                contentAlignment = Alignment.Center
            ) {
                ImagenCatalogo(
                    referencia = nombre,
                    contentDescription = "Foto ${indice + 1}",
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// FlowRow es como un Row que "salta de línea" cuando los chips ya no caben en el ancho.
@Composable
private fun ListaIncluye(incluye: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        incluye.forEach { item ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TarjetaUbicacion(experiencia: Experiencia) {
    // LocalUriHandler abre un enlace con la app que el sistema tenga para él: Google Maps si
    // está instalada, o el navegador si no.
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = experiencia.ubicacion,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (experiencia.latitud != null && experiencia.longitud != null) {
                    Text(
                        text = "%.4f, %.4f".format(experiencia.latitud, experiencia.longitud),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = {
                // Si no hay ninguna app capaz de abrir el enlace, openUri lanza una excepción;
                // runCatching evita que la app se cierre por eso.
                runCatching { uriHandler.openUri(urlMapa(experiencia)) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Filled.Map, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver en mapa")
        }
    }
}

// URL universal de Google Maps: con coordenadas es exacta; sin ellas, busca por el texto
// de la ubicación. Uri.encode convierte espacios y tildes a un formato válido para una URL.
private fun urlMapa(experiencia: Experiencia): String {
    val consulta = if (experiencia.latitud != null && experiencia.longitud != null) {
        "${experiencia.latitud},${experiencia.longitud}"
    } else {
        "${experiencia.titulo}, ${experiencia.ubicacion}"
    }
    return "https://www.google.com/maps/search/?api=1&query=${Uri.encode(consulta)}"
}
