package com.example.ligortravel.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.BuildConfig
import com.example.ligortravel.ui.components.CampoPassword

@Composable
fun PerfilScreen(
    onCerrarSesion: () -> Unit,
    onIrAMisReservas: () -> Unit,
    onAcercaDe: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            EncabezadoPerfil(
                editando = uiState.editando,
                onEditar = {
                    if (uiState.editando) viewModel.cancelarEdicion() else viewModel.activarEdicion()
                }

            )

            Spacer(modifier = Modifier.height(20.dp))
            FotoPortadaPlaceholder()

            Spacer(modifier = Modifier.height(16.dp))
            AvatarYNombre(nombre = uiState.nombre)

            Spacer(modifier = Modifier.height(24.dp))

            CampoCuenta(
                etiqueta = "NOMBRE",
                valor = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                habilitado = uiState.editando,
                icono = Icons.Filled.Person
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampoCuenta(
                etiqueta = "CORREO",
                valor = uiState.email,
                onValueChange = {},
                habilitado = false,
                icono = Icons.Filled.Email
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampoCuenta(
                etiqueta = "TELÉFONO",
                valor = uiState.telefono,
                onValueChange = viewModel::onTelefonoChange,
                habilitado = uiState.editando,
                icono = Icons.Filled.Phone,
                tipoTeclado = KeyboardType.Phone
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampoCuenta(
                etiqueta = "CIUDAD",
                valor = uiState.ciudad,
                onValueChange = viewModel::onCiudadChange,
                habilitado = uiState.editando,
                icono = Icons.Filled.Apartment

            )

            uiState.error?.let { mensaje ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = mensaje, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            if (uiState.guardadoExitoso) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Cambios guardados", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }

            if (uiState.editando) {
                Spacer(modifier = Modifier.height(16.dp))
                BotonGuardar(cargando = uiState.cargando, onClick = viewModel::guardarCambios)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onIrAMisReservas,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.CardTravel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mis reservas")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAcercaDe,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Acerca de LigorTravel")
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    viewModel.abrirDialogoPassword()
                },
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ){
                Text("Actualizar contraseña")
            }

            if (uiState.mostrarDialogo) {
                DialogoActualizarPassword(uiState = uiState, viewModel = viewModel)
            }

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = {
                    viewModel.cerrarSesion()
                    onCerrarSesion()
                },
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "LigorTravel v${BuildConfig.VERSION_NAME}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EncabezadoPerfil(editando: Boolean, onEditar: () -> Unit)  {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "LIGOR TRAVEL",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )
        IconButton(onClick = onEditar) {
            Icon(
                imageVector = if (editando) Icons.Filled.Close else Icons.Filled.Edit,
                contentDescription = if (editando) "Cancelar edición" else "Editar perfil",
                tint = MaterialTheme.colorScheme.onBackground
            )

        }
    }
}

@Composable
private fun FotoPortadaPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Foto de portada",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AvatarYNombre(nombre: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = nombre.ifBlank { "Usuario" },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CampoCuenta(
    etiqueta: String,
    valor: String,
    onValueChange: (String) -> Unit,
    habilitado: Boolean,
    icono: ImageVector,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = etiqueta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            enabled = habilitado,
            singleLine = true,
            leadingIcon = { Icon(icono, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BotonGuardar(cargando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Guardar cambios")
        }
    }
}

@Composable
private fun DialogoActualizarPassword(uiState: PerfilUiState, viewModel: PerfilViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::cerrarDialogoPassword,
        title = { Text("Actualizar contraseña") },
        text = {
            Column {
                CampoPassword(
                    value = uiState.passwordActual,
                    onValueChange = viewModel::onPasswordActualChange,
                    placeholder = "Contraseña actual"
                )
                Spacer(modifier = Modifier.height(12.dp))
                CampoPassword(
                    value = uiState.passwordNuevo,
                    onValueChange = viewModel::onPasswordNuevoChange,
                    placeholder = "Nueva contraseña"
                )
                Spacer(modifier = Modifier.height(12.dp))
                CampoPassword(
                    value = uiState.confirmarPassword,
                    onValueChange = viewModel::onConfirmarPasswordNuevoChange,
                    placeholder = "Confirmar nueva contraseña"
                )
                uiState.errorPassword?.let { mensaje ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = mensaje, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::actualizarPassword,
                enabled = !uiState.cargandoPassword
            ) {
                if (uiState.cargandoPassword) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cerrarDialogoPassword) {
                Text("Cancelar")
            }
        }
    )
}
