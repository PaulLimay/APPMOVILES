package com.example.ligortravel.ui.theme

import androidx.compose.material3.darkColorScheme

// Paleta oscura de LigorTravel (Bocetos/1c.png). Es el colorScheme que aplica LigorTravelTheme
// a toda la app; los valores hex viven en Color.kt.
val LigorDarkColorScheme = darkColorScheme(
    primary = NaranjaAtardecer,
    onPrimary = TextoSobreAcento,
    secondary = TurquesaMar,
    onSecondary = TextoSobreAcento,
    tertiary = VerdeNaturaleza,
    onTertiary = TextoSobreAcento,
    background = FondoOscuro,
    onBackground = TextoPrincipal,
    surface = SuperficieOscura,
    onSurface = TextoPrincipal,
    onSurfaceVariant = TextoSecundario,
    error = RojoError
)
