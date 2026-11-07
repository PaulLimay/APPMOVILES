package com.example.ligortravel.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.data.MensajeChat
import com.example.ligortravel.data.Recomendacion
import com.example.ligortravel.data.TipoRecomendacion
import com.example.ligortravel.ui.components.BotonVolver
import com.example.ligortravel.ui.components.ImagenCatalogo

private val sugerencias = listOf(
    "Quiero un viaje de aventura",
    "Busco algo económico cerca de Lima",
    "¿Qué paquete me recomiendas para Cusco?"
)

@Composable
fun ChatScreen(
    onVolver: () -> Unit,
    onAbrirDestino: (experienciaId: String) -> Unit,
    onAbrirPaquete: (paqueteId: String) -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Un elemento fijo de bienvenida + los mensajes + "escribiendo" o el error, si hay.
    val totalElementos = 1 + uiState.mensajes.size +
        (if (uiState.cargando) 1 else 0) + (if (uiState.error != null) 1 else 0)

    LaunchedEffect(totalElementos) {
        listState.animateScrollToItem(totalElementos - 1)
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Encabezado(onVolver = onVolver)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Bienvenida(
                        mostrarSugerencias = uiState.mensajes.isEmpty(),
                        onSugerencia = viewModel::enviarTexto
                    )
                }
                items(uiState.mensajes) { mensaje ->
                    Mensaje(
                        mensaje = mensaje,
                        onAbrirDestino = onAbrirDestino,
                        onAbrirPaquete = onAbrirPaquete
                    )
                }
                if (uiState.cargando) {
                    item { Escribiendo() }
                }
                uiState.error?.let { error ->
                    item { ErrorChat(mensaje = error, onReintentar = viewModel::reintentar) }
                }
            }

            BarraMensaje(
                texto = uiState.texto,
                habilitado = !uiState.cargando,
                onTextoChange = viewModel::onTextoChange,
                onEnviar = viewModel::enviar
            )
        }
    }
}

@Composable
private fun Encabezado(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonVolver(onClick = onVolver)
        Spacer(modifier = Modifier.width(10.dp))
        AvatarViajero()
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "Ligor",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu compañero de viaje",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AvatarViajero() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Hiking,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun Bienvenida(mostrarSugerencias: Boolean, onSugerencia: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Burbuja(
            texto = "¡Hola! Soy Ligor. Cuéntame qué tipo de viaje buscas y te recomiendo destinos y paquetes.",
            esDelUsuario = false
        )
        Text(
            text = "Tus mensajes se procesan con Gemini de Google y pueden ser revisados. No escribas datos personales ni sensibles.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        if (mostrarSugerencias) {
            sugerencias.forEach { sugerencia ->
                Text(
                    text = sugerencia,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onSugerencia(sugerencia) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun Mensaje(
    mensaje: MensajeChat,
    onAbrirDestino: (String) -> Unit,
    onAbrirPaquete: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Burbuja(texto = mensaje.texto, esDelUsuario = mensaje.esDelUsuario)
        mensaje.recomendaciones.forEach { recomendacion ->
            TarjetaRecomendacion(
                recomendacion = recomendacion,
                onClick = {
                    when (recomendacion.tipo) {
                        TipoRecomendacion.DESTINO -> onAbrirDestino(recomendacion.id)
                        TipoRecomendacion.PAQUETE -> onAbrirPaquete(recomendacion.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun Burbuja(texto: String, esDelUsuario: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (esDelUsuario) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = texto,
            color = if (esDelUsuario) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontSize = 14.sp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (esDelUsuario) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun TarjetaRecomendacion(recomendacion: Recomendacion, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImagenRecomendacion(nombre = recomendacion.imagen)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (recomendacion.tipo == TipoRecomendacion.PAQUETE) "Paquete" else "Destino",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp
            )
            Text(
                text = recomendacion.titulo,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            if (recomendacion.detalle.isNotBlank()) {
                Text(
                    text = recomendacion.detalle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Text(
                text = if (recomendacion.tipo == TipoRecomendacion.PAQUETE) {
                    "S/ ${recomendacion.precio} por persona"
                } else {
                    "Desde S/ ${recomendacion.precio}"
                },
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ImagenRecomendacion(nombre: String?) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        ImagenCatalogo(
            referencia = nombre,
            contentDescription = null,
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

@Composable
private fun Escribiendo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Ligor está escribiendo...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ErrorChat(mensaje: String, onReintentar: () -> Unit) {
    Column {
        Text(text = mensaje, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        TextButton(onClick = onReintentar) {
            Text("Reintentar", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun BarraMensaje(
    texto: String,
    habilitado: Boolean,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = onTextoChange,
            placeholder = { Text("Pregúntale a Ligor...") },
            shape = RoundedCornerShape(20.dp),
            maxLines = 4,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = onEnviar, enabled = habilitado && texto.isNotBlank()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Enviar mensaje",
                tint = if (habilitado && texto.isNotBlank()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
