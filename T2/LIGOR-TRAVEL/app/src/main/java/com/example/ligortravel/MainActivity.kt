package com.example.ligortravel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ligortravel.ui.navigation.LigorNavGraph
import com.example.ligortravel.ui.theme.LigorTravelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LigorTravelTheme {
                LigorNavGraph()
            }
        }
    }
}
