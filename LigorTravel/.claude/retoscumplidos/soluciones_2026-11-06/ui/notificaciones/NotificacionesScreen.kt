package com.example.ligortravel.ui.notificaciones

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ligortravel.ui.components.BotonVolver

@Composable
fun NotificacionesScreen(
    onVolver: () -> Unit
) {
    val mensaje = "No tienes notificaciones nuevas"

    Surface(color = MaterialTheme.colorScheme.background) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Encabezado(onVolver = onVolver)
            Text(mensaje)

        }
    }

}

@Composable
private fun Encabezado(onVolver: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        BotonVolver(onVolver)
        Spacer(modifier = Modifier.width(12.dp))
        Column{
            Text(
                text = "Notificaciones",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

}