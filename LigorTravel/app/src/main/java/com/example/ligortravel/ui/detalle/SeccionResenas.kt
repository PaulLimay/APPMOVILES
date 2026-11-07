package com.example.ligortravel.ui.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.Resena
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-PE"))

// Sección "Reseñas" de la ficha del destino. Recibe el ViewModel, igual que FormularioLogin en
// AuthScreen, porque usa varias de sus acciones.
@Composable
fun SeccionResenas(experiencia: Experiencia, uiState: DetalleUiState, viewModel: DetalleViewModel) {
    var resenaAEliminar by remember { mutableStateOf<Resena?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ResumenCalificacion(experiencia, uiState.distribucion)

        val mia = uiState.miResena
        when {
            uiState.formularioAbierto -> FormularioResena(
                estrellas = uiState.estrellas,
                comentario = uiState.comentario,
                editando = mia != null,
                guardando = uiState.guardandoResena,
                error = uiState.errorResena,
                onEstrellasChange = viewModel::onEstrellasChange,
                onComentarioChange = viewModel::onComentarioChange,
                onPublicar = viewModel::guardarResena,
                onCancelar = viewModel::cerrarFormulario
            )
            mia != null -> {
                TarjetaResena(
                    resena = mia,
                    titulo = "Tu reseña",
                    acciones = {
                        IconButton(onClick = viewModel::abrirFormulario, enabled = uiState.reservaHabilitante != null) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar mi reseña", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { resenaAEliminar = mia }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar mi reseña", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                uiState.errorResena?.let { TextoError(it) }
            }
            uiState.reservaHabilitante != null -> Button(onClick = viewModel::abrirFormulario) {
                Icon(Icons.Filled.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escribir una reseña")
            }
            !uiState.verificandoViaje -> Text(
                text = "Podrás dejar tu reseña después de visitar este destino con un paquete de LigorTravel.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }

        if (uiState.resenas.isEmpty() && mia == null) {
            Text(
                text = "Aún no hay reseñas de este destino.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        uiState.resenas.forEach { resena ->
            TarjetaResena(
                resena = resena,
                titulo = null,
                // El administrador puede moderar reseñas inapropiadas.
                acciones = if (uiState.esAdmin) {
                    {
                        IconButton(onClick = { resenaAEliminar = resena }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar reseña", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    null
                }
            )
        }
    }

    resenaAEliminar?.let { resena ->
        AlertDialog(
            onDismissRequest = { resenaAEliminar = null },
            title = { Text(if (resena.id == uiState.miResena?.id) "¿Borrar tu reseña?" else "¿Borrar esta reseña?") },
            text = { Text("El promedio del destino se recalculará. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarResena(resena)
                    resenaAEliminar = null
                }) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { resenaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// Promedio grande + barras con cuántas reseñas tiene cada número de estrellas.
@Composable
private fun ResumenCalificacion(experiencia: Experiencia, distribucion: Map<Int, Int>) {
    val total = experiencia.totalResenas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
            Text(
                text = ReglasResenas.textoPromedio(experiencia),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = if (total > 0) 34.sp else 22.sp,
                fontWeight = FontWeight.Bold
            )
            val promedio = ReglasResenas.promedio(experiencia.sumaEstrellas, total) ?: 0.0
            FilaEstrellas(estrellas = Math.round(promedio).toInt(), tamano = 14.dp)
            Text(
                text = if (total == 1) "1 reseña" else "$total reseñas",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val conteoTotal = distribucion.values.sum()
            for (n in ReglasResenas.ESTRELLAS_MAX downTo ReglasResenas.ESTRELLAS_MIN) {
                val cantidad = distribucion[n] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$n", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.width(14.dp))
                    LinearProgressIndicator(
                        progress = { if (conteoTotal == 0) 0f else cantidad.toFloat() / conteoTotal },
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun FormularioResena(
    estrellas: Int,
    comentario: String,
    editando: Boolean,
    guardando: Boolean,
    error: String?,
    onEstrellasChange: (Int) -> Unit,
    onComentarioChange: (String) -> Unit,
    onPublicar: () -> Unit,
    onCancelar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = if (editando) "Edita tu reseña" else "¿Qué te pareció este destino?",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        // Estrellas tocables: tocar la tercera deja 3 estrellas.
        Row {
            for (n in 1..ReglasResenas.ESTRELLAS_MAX) {
                IconButton(onClick = { onEstrellasChange(n) }, enabled = !guardando) {
                    Icon(
                        imageVector = if (n <= estrellas) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "$n estrellas",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        OutlinedTextField(
            value = comentario,
            onValueChange = onComentarioChange,
            label = { Text("Tu reseña") },
            supportingText = { Text("${comentario.length}/${ReglasResenas.COMENTARIO_MAX}") },
            minLines = 3,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        )
        error?.let { TextoError(it) }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPublicar, enabled = !guardando) {
                if (guardando) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(if (editando) "Guardar cambios" else "Publicar")
                }
            }
            OutlinedButton(onClick = onCancelar, enabled = !guardando) { Text("Cancelar") }
        }
    }
}

@Composable
private fun TarjetaResena(resena: Resena, titulo: String?, acciones: (@Composable () -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                if (titulo != null) {
                    Text(text = titulo, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = resena.nombre,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilaEstrellas(estrellas = resena.estrellas, tamano = 14.dp)
                    val fecha = resena.actualizadaEn ?: resena.creadaEn
                    if (fecha != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            // Un instante (no un día elegido): se muestra en la zona del celular.
                            text = fecha.toDate().toInstant().atZone(ZoneId.systemDefault()).format(formatoFecha) +
                                if (resena.actualizadaEn != null) " (editada)" else "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            acciones?.invoke()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = resena.comentario,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(end = 10.dp)
        )
    }
}

@Composable
private fun FilaEstrellas(estrellas: Int, tamano: Dp) {
    Row {
        for (n in 1..ReglasResenas.ESTRELLAS_MAX) {
            Icon(
                imageVector = if (n <= estrellas) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(tamano)
            )
        }
    }
}

@Composable
private fun TextoError(texto: String) {
    Text(text = texto, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
}
