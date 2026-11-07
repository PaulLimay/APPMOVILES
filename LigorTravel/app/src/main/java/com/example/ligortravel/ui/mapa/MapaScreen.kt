package com.example.ligortravel.ui.mapa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.ui.components.BotonVolver
import com.example.ligortravel.ui.components.ImagenCatalogo
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

// Mapa oscuro de OpenFreeMap (datos de OpenStreetMap): gratis, sin API key ni facturación.
private const val ESTILO_MAPA = "https://tiles.openfreemap.org/styles/dark"
private const val FUENTE_DESTINOS = "destinos"
private const val CAPA_DESTINOS = "capa-destinos"
private const val ICONO_MARCADOR = "marcador-destino"

@Composable
fun MapaScreen(
    onVolver: () -> Unit,
    onAbrirDestino: (experienciaId: String) -> Unit,
    viewModel: MapaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorMarcador = MaterialTheme.colorScheme.primary.toArgb()
    val colorBorde = MaterialTheme.colorScheme.onPrimary.toArgb()

    // Permiso de ubicación: se pide al entrar. Si el turista lo rechaza, el mapa sigue
    // funcionando, solo que sin su posición ni la distancia a cada destino.
    var permisoUbicacion by remember { mutableStateOf(tienePermisoUbicacion(context)) }
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { respuesta -> permisoUbicacion = respuesta.values.any { it } }
    LaunchedEffect(Unit) {
        if (!permisoUbicacion) {
            pedirPermiso.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    val mapView = rememberMapViewConCicloDeVida()
    // El mapa y su estilo llegan de forma asíncrona (getMapAsync / setStyle): se guardan como
    // estado para que los efectos de abajo se vuelvan a ejecutar cuando estén listos.
    var mapa by remember { mutableStateOf<MapLibreMap?>(null) }
    var estilo by remember { mutableStateOf<Style?>(null) }
    var camaraAjustada by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<String?>(null) }

    // Configuración inicial del mapa: una sola vez.
    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            mapa = map
            map.setStyle(Style.Builder().fromUri(ESTILO_MAPA)) { style ->
                style.addImage(ICONO_MARCADOR, crearMarcador(context, colorMarcador, colorBorde))
                style.addSource(GeoJsonSource(FUENTE_DESTINOS))
                style.addLayer(
                    SymbolLayer(CAPA_DESTINOS, FUENTE_DESTINOS).withProperties(
                        PropertyFactory.iconImage(ICONO_MARCADOR),
                        // Que se vean todos los marcadores aunque estén cerca unos de otros.
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true)
                    )
                )
                estilo = style
            }
            // Al tocar el mapa se busca si debajo del dedo hay un marcador de la capa de destinos.
            map.addOnMapClickListener { punto ->
                val enPantalla = map.projection.toScreenLocation(punto)
                val tocado = map.queryRenderedFeatures(enPantalla, CAPA_DESTINOS).firstOrNull()
                viewModel.seleccionar(tocado?.getStringProperty("id"))
                true
            }
        }
    }

    // Marcadores: se redibujan cada vez que cambian los destinos (tiempo real).
    LaunchedEffect(estilo, uiState.destinos) {
        val style = estilo ?: return@LaunchedEffect
        val features = uiState.destinos.map { destino ->
            Feature.fromGeometry(Point.fromLngLat(destino.longitud!!, destino.latitud!!)).apply {
                addStringProperty("id", destino.id)
            }
        }
        style.getSourceAs<GeoJsonSource>(FUENTE_DESTINOS)?.setGeoJson(FeatureCollection.fromFeatures(features))
        val map = mapa
        if (!camaraAjustada && map != null && uiState.destinos.isNotEmpty()) {
            encuadrar(map, uiState.destinos)
            camaraAjustada = true
        }
    }

    // Punto azul de "estás aquí": cuando el estilo está listo y hay permiso.
    LaunchedEffect(estilo, permisoUbicacion) {
        val style = estilo
        val map = mapa
        if (style != null && map != null && permisoUbicacion) activarUbicacion(context, map, style)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonVolver(onClick = onVolver)
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "${uiState.destinos.size} destinos en el mapa",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        if (uiState.cargando) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    aviso = null
                    when {
                        !permisoUbicacion -> pedirPermiso.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                        else -> {
                            val ubicacion = mapa?.let(::ubicacionActual)
                            if (ubicacion == null) {
                                aviso = "Buscando tu ubicación... revisa que el GPS esté activado"
                            } else {
                                mapa?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(ubicacion.latitud, ubicacion.longitud), 11.0)
                                )
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "Mi ubicación")
            }

            val mensaje = uiState.error ?: aviso ?: if (!permisoUbicacion) {
                "Activa el permiso de ubicación para ver qué tan lejos estás de cada destino"
            } else {
                null
            }
            if (mensaje != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensaje,
                        color = if (uiState.error != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            uiState.seleccionado?.let { destino ->
                // La distancia se calcula al elegir el destino, con la última ubicación conocida.
                val distancia = remember(destino.id) {
                    val ubicacion = mapa?.let(::ubicacionActual)
                    ubicacion?.let {
                        ReglasMapa.textoDistancia(
                            ReglasMapa.distanciaKm(it, Coordenada(destino.latitud!!, destino.longitud!!))
                        )
                    }
                }
                TarjetaDestino(
                    destino = destino,
                    distancia = distancia,
                    onVerFicha = { onAbrirDestino(destino.id) },
                    onCerrar = { viewModel.seleccionar(null) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaDestino(
    destino: Experiencia,
    distancia: String?,
    onVerFicha: () -> Unit,
    onCerrar: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                ImagenCatalogo(
                    referencia = destino.imagen,
                    contentDescription = destino.titulo,
                    modifier = Modifier.fillMaxSize()
                ) {
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
                    text = destino.titulo,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = destino.ubicacion,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                if (distancia != null) {
                    Text(text = distancia, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Desde S/ ${destino.precioDesde}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onVerFicha) { Text("Ver ficha") }
                }
            }
            IconButton(onClick = onCerrar, modifier = Modifier.align(Alignment.Top)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// El MapView necesita enterarse del ciclo de vida de la pantalla (onStart, onResume...) para
// pausar el dibujo y liberar memoria. Este observador se los reenvía.
@Composable
private fun rememberMapViewConCicloDeVida(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply { onCreate(null) }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observador)
        onDispose {
            lifecycle.removeObserver(observador)
            // Si se sale de la pantalla con la app visible, el mapa sigue "iniciado": se detiene
            // en orden antes de destruirlo.
            val estado = lifecycle.currentState
            if (estado.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
            if (estado.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
            mapView.onDestroy()
        }
    }
    return mapView
}

private fun tienePermisoUbicacion(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

// Solo se llama después de comprobar el permiso (ver el efecto que usa permisoUbicacion).
@SuppressLint("MissingPermission")
private fun activarUbicacion(context: Context, map: MapLibreMap, style: Style) {
    val componente = map.locationComponent
    if (!componente.isLocationComponentActivated) {
        // El motor de ubicación por defecto usa el servicio de ubicación de Android: no
        // necesita Google Play Services.
        componente.activateLocationComponent(
            LocationComponentActivationOptions.builder(context, style)
                .useDefaultLocationEngine(true)
                .build()
        )
    }
    componente.isLocationComponentEnabled = true
    componente.renderMode = RenderMode.COMPASS
}

private fun ubicacionActual(map: MapLibreMap): Coordenada? {
    val componente = map.locationComponent
    if (!componente.isLocationComponentActivated) return null
    val ubicacion = componente.lastKnownLocation ?: return null
    return Coordenada(ubicacion.latitude, ubicacion.longitude)
}

// Ajusta la cámara para que se vean todos los destinos.
private fun encuadrar(map: MapLibreMap, destinos: List<Experiencia>) {
    val puntos = destinos.map { LatLng(it.latitud!!, it.longitud!!) }
    val actualizacion = if (puntos.size == 1) {
        CameraUpdateFactory.newLatLngZoom(puntos.first(), 10.0)
    } else {
        CameraUpdateFactory.newLatLngBounds(LatLngBounds.Builder().includes(puntos).build(), 120)
    }
    map.moveCamera(actualizacion)
}

// Marcador circular naranja con borde, dibujado en un Bitmap (MapLibre usa imágenes, no
// composables, para los íconos de una capa).
private fun crearMarcador(context: Context, relleno: Int, borde: Int): Bitmap {
    val densidad = context.resources.displayMetrics.density
    val tamano = (26 * densidad).toInt()
    val bitmap = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val pincel = Paint(Paint.ANTI_ALIAS_FLAG)
    val centro = tamano / 2f
    pincel.color = borde
    canvas.drawCircle(centro, centro, centro, pincel)
    pincel.color = relleno
    canvas.drawCircle(centro, centro, centro - 3 * densidad, pincel)
    return bitmap
}
