package com.example.ligortravel.ui.home

import androidx.annotation.DrawableRes
import com.example.ligortravel.R

// Traduce el campo "imagen" que viene de Firestore (texto) al drawable local correspondiente.
// Si agregas un destino nuevo con foto, suma aquí su archivo de res/drawable.
private val imagenesPorNombre = mapOf(
    "laguna_69" to R.drawable.laguna_69,
    "vinicunca" to R.drawable.vinicunca,
    "machu_picchu" to R.drawable.machu_picchu,
    "valle_sagrado" to R.drawable.valle_sagrado,
    "cusco_esencial" to R.drawable.cusco_esencial,
    "canon_colca" to R.drawable.canon_colca,
    "islas_ballestas" to R.drawable.islas_ballestas,
    "ruta_pisco" to R.drawable.ruta_pisco,
    "lunahuana" to R.drawable.lunahuana,
    "pachacamac" to R.drawable.pachacamac,
    "laguna_churup" to R.drawable.laguna_churup,
    "norte_chico" to R.drawable.norte_chico,
    "pesca_artesanal" to R.drawable.pesca_artesanal,
    "lima_nocturna" to R.drawable.lima_nocturna,
    "feria_artesanal" to R.drawable.feria_artesanal,
    // Galería de la pantalla de detalle: 3 fotos extra por destino (créditos en CREDITOS_FOTOS.md).
    "laguna_69_1" to R.drawable.laguna_69_1,
    "laguna_69_2" to R.drawable.laguna_69_2,
    "laguna_69_3" to R.drawable.laguna_69_3,
    "vinicunca_1" to R.drawable.vinicunca_1,
    "vinicunca_2" to R.drawable.vinicunca_2,
    "vinicunca_3" to R.drawable.vinicunca_3,
    "machu_picchu_1" to R.drawable.machu_picchu_1,
    "machu_picchu_2" to R.drawable.machu_picchu_2,
    "machu_picchu_3" to R.drawable.machu_picchu_3,
    "valle_sagrado_1" to R.drawable.valle_sagrado_1,
    "valle_sagrado_2" to R.drawable.valle_sagrado_2,
    "valle_sagrado_3" to R.drawable.valle_sagrado_3,
    "cusco_esencial_1" to R.drawable.cusco_esencial_1,
    "cusco_esencial_2" to R.drawable.cusco_esencial_2,
    "cusco_esencial_3" to R.drawable.cusco_esencial_3,
    "canon_colca_1" to R.drawable.canon_colca_1,
    "canon_colca_2" to R.drawable.canon_colca_2,
    "canon_colca_3" to R.drawable.canon_colca_3,
    "islas_ballestas_1" to R.drawable.islas_ballestas_1,
    "islas_ballestas_2" to R.drawable.islas_ballestas_2,
    "islas_ballestas_3" to R.drawable.islas_ballestas_3,
    "ruta_pisco_1" to R.drawable.ruta_pisco_1,
    "ruta_pisco_2" to R.drawable.ruta_pisco_2,
    "ruta_pisco_3" to R.drawable.ruta_pisco_3,
    "lunahuana_1" to R.drawable.lunahuana_1,
    "lunahuana_2" to R.drawable.lunahuana_2,
    "lunahuana_3" to R.drawable.lunahuana_3,
    "pachacamac_1" to R.drawable.pachacamac_1,
    "pachacamac_2" to R.drawable.pachacamac_2,
    "pachacamac_3" to R.drawable.pachacamac_3,
    "laguna_churup_1" to R.drawable.laguna_churup_1,
    "laguna_churup_2" to R.drawable.laguna_churup_2,
    "laguna_churup_3" to R.drawable.laguna_churup_3,
    "norte_chico_1" to R.drawable.norte_chico_1,
    "norte_chico_2" to R.drawable.norte_chico_2,
    "norte_chico_3" to R.drawable.norte_chico_3,
    "pesca_artesanal_1" to R.drawable.pesca_artesanal_1,
    "pesca_artesanal_2" to R.drawable.pesca_artesanal_2,
    "pesca_artesanal_3" to R.drawable.pesca_artesanal_3,
    "lima_nocturna_1" to R.drawable.lima_nocturna_1,
    "lima_nocturna_2" to R.drawable.lima_nocturna_2,
    "lima_nocturna_3" to R.drawable.lima_nocturna_3,
    "feria_artesanal_1" to R.drawable.feria_artesanal_1,
    "feria_artesanal_2" to R.drawable.feria_artesanal_2,
    "feria_artesanal_3" to R.drawable.feria_artesanal_3
)

// Devuelve null si el nombre no existe (o es null): la tarjeta muestra el placeholder.
@DrawableRes
fun imagenDeExperiencia(nombre: String?): Int? = imagenesPorNombre[nombre]
