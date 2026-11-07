package com.example.ligortravel.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.R
import com.example.ligortravel.ui.components.CampoPassword
import com.example.ligortravel.ui.components.TabPill
import com.example.ligortravel.ui.login.LoginUiState
import com.example.ligortravel.ui.login.LoginViewModel
import com.example.ligortravel.ui.registro.RegistroUiState
import com.example.ligortravel.ui.registro.RegistroViewModel

private enum class AuthTab { LOGIN, REGISTRO }

@Composable
fun AuthScreen(
    onAutenticado: () -> Unit,
    loginViewModel: LoginViewModel = viewModel(),
    registroViewModel: RegistroViewModel = viewModel()
) {
    var tabSeleccionada by remember { mutableStateOf(AuthTab.LOGIN) }

    val loginState by loginViewModel.uiState.collectAsState()
    val registroState by registroViewModel.uiState.collectAsState()

    LaunchedEffect(loginState.loginExitoso) {
        if (loginState.loginExitoso) onAutenticado()
    }
    LaunchedEffect(registroState.registroExitoso) {
        if (registroState.registroExitoso) onAutenticado()
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            EncabezadoAuth()
            Spacer(modifier = Modifier.height(20.dp))
            FotoExperienciaLogin()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "TURISMO",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Entra y reserva",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp
            )
            Text(
                text = "lo que se recuerda",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            SelectorTabs(
                seleccionada = tabSeleccionada,
                onSeleccionar = { tabSeleccionada = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (tabSeleccionada) {
                AuthTab.LOGIN -> FormularioLogin(loginState, loginViewModel)
                AuthTab.REGISTRO -> FormularioRegistro(registroState, registroViewModel)
            }

            Spacer(modifier = Modifier.height(20.dp))
            DivisorOContinuaCon()
            Spacer(modifier = Modifier.height(12.dp))
            BotonesSocial()

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Al continuar aceptas los Términos y la Política de privacidad de Ligor.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EncabezadoAuth() {
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
        OutlinedButton(
            onClick = { /* Explorar sin cuenta: pendiente de definir flujo de invitado */ },
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Explorar sin cuenta", fontSize = 12.sp)
        }
    }
}

@Composable
private fun FotoExperienciaLogin() {
    Image(
        painter = painterResource(R.drawable.lima),
        contentDescription = "Plaza de Armas de Lima",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
private fun SelectorTabs(seleccionada: AuthTab, onSeleccionar: (AuthTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .padding(4.dp)
    ) {
        TabPill(
            texto = "Ingresar",
            seleccionada = seleccionada == AuthTab.LOGIN,
            modifier = Modifier.weight(1f)
        ) { onSeleccionar(AuthTab.LOGIN) }
        TabPill(
            texto = "Crear cuenta",
            seleccionada = seleccionada == AuthTab.REGISTRO,
            modifier = Modifier.weight(1f)
        ) { onSeleccionar(AuthTab.REGISTRO) }
    }
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    )
}

@Composable
private fun FormularioLogin(state: LoginUiState, viewModel: LoginViewModel) {
    Column {
        EtiquetaCampo("CORREO")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = { Text("usuario@correo.com") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EtiquetaCampo("CONTRASEÑA")
            TextButton(onClick = { /* Recuperar contraseña: pendiente */ }) {
                Text("¿Olvidaste?", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        CampoPassword(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Mínimo 6 caracteres"
        )

        state.error?.let { mensaje ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        BotonPrincipal(
            texto = "Iniciar sesión",
            cargando = state.cargando,
            onClick = viewModel::iniciarSesion
        )
    }
}

@Composable
private fun FormularioRegistro(state: RegistroUiState, viewModel: RegistroViewModel) {
    Column {
        EtiquetaCampo("NOMBRE")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = state.nombre,
            onValueChange = viewModel::onNombreChange,
            placeholder = { Text("Tu nombre") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        EtiquetaCampo("CORREO")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = { Text("usuario@correo.com") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        EtiquetaCampo("TELÉFONO")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = state.telefono,
            onValueChange = viewModel::onTelefonoChange,
            placeholder = { Text("999 999 999") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        EtiquetaCampo("CONTRASEÑA")
        Spacer(modifier = Modifier.height(4.dp))
        CampoPassword(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Mínimo 6 caracteres"
        )

        Spacer(modifier = Modifier.height(12.dp))
        EtiquetaCampo("CONFIRMAR CONTRASEÑA")
        Spacer(modifier = Modifier.height(4.dp))
        CampoPassword(
            value = state.confirmarPassword,
            onValueChange = viewModel::onConfirmarPasswordChange,
            placeholder = "Repite tu contraseña"
        )

        state.error?.let { mensaje ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        BotonPrincipal(
            texto = "Crear cuenta",
            cargando = state.cargando,
            onClick = viewModel::registrar
        )
    }
}

@Composable
private fun BotonPrincipal(texto: String, cargando: Boolean, onClick: () -> Unit) {
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
            Text(text = texto)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun DivisorOContinuaCon() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "  o continúa con  ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BotonesSocial() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = { /* Login con Google: pendiente */ },
            modifier = Modifier.weight(1f)
        ) {
            CirculoIcono(texto = "G")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Google")
        }
        OutlinedButton(
            onClick = { /* Login con Apple: pendiente */ },
            modifier = Modifier.weight(1f)
        ) {
            CirculoIcono(texto = "")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apple")
        }
    }
}

@Composable
private fun CirculoIcono(texto: String) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (texto.isNotEmpty()) {
            Text(text = texto, color = MaterialTheme.colorScheme.surface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
