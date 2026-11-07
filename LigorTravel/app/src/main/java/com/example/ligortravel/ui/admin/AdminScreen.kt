package com.example.ligortravel.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.ui.components.ImagenCatalogo
import com.example.ligortravel.ui.components.PantallaEstado
import com.example.ligortravel.ui.components.TabPill

private enum class PestanaAdmin { DESTINOS, PAQUETES, RESERVAS }

@Composable
fun AdminScreen(
    onVolver: () -> Unit,
    onEditarDestino: (destinoId: String?) -> Unit,
    onEditarPaquete: (paqueteId: String?) -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // La pestaña elegida es estado de la UI (igual que en Mis reservas), no del ViewModel.
    var pestana by rememberSaveable { mutableStateOf(PestanaAdmin.DESTINOS) }
    var destinoAEliminar by remember { mutableStateOf<Experiencia?>(null) }
    var paqueteAEliminar by remember { mutableStateOf<Paquete?>(null) }

    Surface(color = MaterialTheme.colorScheme.background) {
        when (uiState.esAdmin) {
            null -> PantallaEstado(onVolver) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            false -> PantallaEstado(onVolver) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = uiState.error ?: "Acceso restringido: solo para administradores",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
            true -> Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    EncabezadoAdmin(titulo = "Administración", onVolver = onVolver)
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        // Sin contadores: con tres pestañas no caben en un celular angosto.
                        TabPill(
                            texto = "Destinos",
                            seleccionada = pestana == PestanaAdmin.DESTINOS,
                            modifier = Modifier.weight(1f)
                        ) { pestana = PestanaAdmin.DESTINOS }
                        TabPill(
                            texto = "Paquetes",
                            seleccionada = pestana == PestanaAdmin.PAQUETES,
                            modifier = Modifier.weight(1f)
                        ) { pestana = PestanaAdmin.PAQUETES }
                        TabPill(
                            texto = "Reservas",
                            seleccionada = pestana == PestanaAdmin.RESERVAS,
                            modifier = Modifier.weight(1f)
                        ) { pestana = PestanaAdmin.RESERVAS }
                    }

                    uiState.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    if (uiState.cargando) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (pestana == PestanaAdmin.RESERVAS) {
                        ReservasAdmin(
                            reservas = uiState.reservas,
                            usuarios = uiState.usuarios,
                            procesandoId = uiState.procesandoReservaId,
                            onRegistrarPago = viewModel::registrarPago,
                            onCancelar = viewModel::cancelarReserva
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (pestana == PestanaAdmin.PAQUETES) {
                                items(uiState.paquetes, key = { it.id }) { paquete ->
                                    FilaCatalogo(
                                        imagen = paquete.imagen,
                                        titulo = paquete.titulo,
                                        detalle = "S/ ${paquete.precio} · " +
                                            if (paquete.cupos > 0) "${paquete.cupos} cupos" else "Agotado",
                                        habilitado = !uiState.eliminando,
                                        onEditar = { onEditarPaquete(paquete.id) },
                                        onEliminar = { paqueteAEliminar = paquete }
                                    )
                                }
                            } else {
                                items(uiState.destinos, key = { it.id }) { destino ->
                                    FilaCatalogo(
                                        imagen = destino.imagen,
                                        titulo = destino.titulo,
                                        detalle = "${destino.ubicacion} · Desde S/ ${destino.precioDesde}",
                                        habilitado = !uiState.eliminando,
                                        onEditar = { onEditarDestino(destino.id) },
                                        onEliminar = { destinoAEliminar = destino }
                                    )
                                }
                            }
                        }
                    }
                }

                // Las reservas las crean los turistas: en esa pestaña no hay botón de agregar.
                if (pestana != PestanaAdmin.RESERVAS) FloatingActionButton(
                    onClick = { if (pestana == PestanaAdmin.PAQUETES) onEditarPaquete(null) else onEditarDestino(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = if (pestana == PestanaAdmin.PAQUETES) "Nuevo paquete" else "Nuevo destino"
                    )
                }
            }
        }
    }

    destinoAEliminar?.let { destino ->
        DialogoEliminar(
            nombre = destino.titulo,
            onConfirmar = {
                viewModel.eliminarDestino(destino)
                destinoAEliminar = null
            },
            onCerrar = { destinoAEliminar = null }
        )
    }
    paqueteAEliminar?.let { paquete ->
        DialogoEliminar(
            nombre = paquete.titulo,
            onConfirmar = {
                viewModel.eliminarPaquete(paquete)
                paqueteAEliminar = null
            },
            onCerrar = { paqueteAEliminar = null }
        )
    }
}

@Composable
private fun FilaCatalogo(
    imagen: String?,
    titulo: String,
    detalle: String,
    habilitado: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            ImagenCatalogo(referencia = imagen, contentDescription = null, modifier = Modifier.fillMaxSize()) {
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
                text = titulo,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = detalle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        IconButton(onClick = onEditar, enabled = habilitado) {
            Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onEliminar, enabled = habilitado) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
        }
    }
}

// Eliminar no se puede deshacer: por eso se confirma antes.
@Composable
private fun DialogoEliminar(nombre: String, onConfirmar: () -> Unit, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("¿Eliminar \"$nombre\"?") },
        text = { Text("Se borrarán también sus fotos. Esta acción no se puede deshacer.") },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}
