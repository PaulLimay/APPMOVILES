package com.example.ligortravel.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.ui.components.PantallaEstado

@Composable
fun EditarPaqueteScreen(
    paqueteId: String?,
    onVolver: () -> Unit,
    viewModel: EditarPaqueteViewModel = viewModel { EditarPaqueteViewModel(paqueteId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val resolver = LocalContext.current.contentResolver
    val elegirPortada = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onFotoElegida(resolver, uri)
    }
    val soloImagenes = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) onVolver()
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        when {
            uiState.cargandoDatos -> PantallaEstado(onVolver) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.errorDatos != null -> PantallaEstado(onVolver) {
                Text(
                    text = uiState.errorDatos.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            else -> Column(modifier = Modifier.fillMaxSize().imePadding()) {
                EncabezadoAdmin(
                    titulo = if (paqueteId == null) "Nuevo paquete" else "Editar paquete",
                    onVolver = onVolver
                )
                val formulario = uiState.formulario
                val cambiar = viewModel::onFormularioChange

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TituloSeccion("Foto de portada")
                    val portada = uiState.portada
                    if (portada == null) {
                        BotonAgregarFoto(
                            texto = "Elegir foto",
                            alto = 180.dp,
                            procesando = uiState.procesandoFoto,
                            modifier = Modifier.fillMaxWidth()
                        ) { elegirPortada.launch(soloImagenes) }
                    } else {
                        MiniaturaFoto(foto = portada, alto = 180.dp, modifier = Modifier.fillMaxWidth())
                        TextButton(
                            onClick = { elegirPortada.launch(soloImagenes) },
                            enabled = !uiState.procesandoFoto
                        ) {
                            Text(if (uiState.procesandoFoto) "Procesando foto..." else "Cambiar foto de portada")
                        }
                    }

                    TituloSeccion("Datos del paquete")
                    CampoAdmin("Nombre", formulario.titulo, { cambiar(formulario.copy(titulo = it)) })
                    CampoAdmin(
                        "Zona o destino",
                        formulario.destino,
                        { cambiar(formulario.copy(destino = it)) },
                        ayuda = "Ej. Cusco o Paracas e Ica"
                    )
                    CampoAdmin(
                        "Duración",
                        formulario.duracion,
                        { cambiar(formulario.copy(duracion = it)) },
                        ayuda = "Ej. 5 días / 4 noches"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CampoAdmin(
                            "Precio por persona (S/)",
                            formulario.precio,
                            { cambiar(formulario.copy(precio = it)) },
                            modifier = Modifier.weight(1f),
                            numerico = true
                        )
                        CampoAdmin(
                            "Cupos disponibles",
                            formulario.cupos,
                            { cambiar(formulario.copy(cupos = it)) },
                            modifier = Modifier.weight(1f),
                            numerico = true
                        )
                    }
                    CampoAdmin(
                        "Descripción",
                        formulario.descripcion,
                        { cambiar(formulario.copy(descripcion = it)) },
                        lineas = 4,
                        ayuda = "${formulario.descripcion.length}/${ReglasAdmin.DESCRIPCION_MAX}"
                    )
                    CampoAdmin(
                        "Servicios incluidos",
                        formulario.serviciosIncluidos,
                        { cambiar(formulario.copy(serviciosIncluidos = it)) },
                        lineas = 3,
                        ayuda = "Uno por línea, ej. Hotel 3 estrellas"
                    )

                    TituloSeccion("Destinos incluidos (${formulario.experienciaIds.size})")
                    uiState.destinosDisponibles.forEach { destino ->
                        FilaDestino(
                            destino = destino,
                            elegido = destino.id in formulario.experienciaIds,
                            onClick = { viewModel.alternarDestino(destino.id) }
                        )
                    }
                }

                BarraGuardar(
                    error = uiState.error,
                    cargando = uiState.cargando,
                    habilitado = !uiState.procesandoFoto,
                    onGuardar = viewModel::guardar
                )
            }
        }
    }
}

@Composable
private fun FilaDestino(destino: Experiencia, elegido: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = elegido, onCheckedChange = { onClick() })
        Column(modifier = Modifier.weight(1f)) {
            Text(text = destino.titulo, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            Text(
                text = "${destino.ubicacion} · S/ ${destino.precioDesde}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}
