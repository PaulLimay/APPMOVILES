package com.example.ligortravel.ui.acercade

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
fun AcercaDeScreen(onVolver: () -> Unit){
    val titulo = "Acerca de LigorTravel"
    val texto = "Versión 1.0"

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxSize()){
            Encabezado(onVolver)
            Text(
                text = "$titulo",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$texto",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

        }
    }
}
@Composable
private fun Encabezado(onVolver: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonVolver(onClick = onVolver)
    }
}