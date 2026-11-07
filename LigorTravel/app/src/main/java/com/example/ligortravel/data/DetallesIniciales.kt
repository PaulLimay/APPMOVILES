package com.example.ligortravel.data

// Siembra temporal: campos de la pantalla de detalle para los 15 destinos que ya están en
// Firestore (las claves son los ids de documento "1".."15"). ExperienciaRepository.
// completarDetallesSiFaltan los sube la primera vez que se abre el Home. Una vez que los datos
// estén en Firestore, borrar este archivo, esa función y la llamada en HomeViewModel.
// "galeria" son nombres de res/drawable registrados en ImagenesExperiencia.kt.
object DetallesIniciales {
    val porId: Map<String, Map<String, Any>> = mapOf(
        "1" to mapOf(
            "galeria" to listOf("laguna_69_1", "laguna_69_2", "laguna_69_3"),
            "descripcion" to "Caminata de unos 14 km ida y vuelta por el Parque Nacional Huascarán, desde Cebollapampa (3.900 m) hasta una laguna de color turquesa a 4.600 m, al pie del nevado Chacraraju. En el camino se pasa por cascadas, praderas altoandinas y una laguna intermedia. Es exigente por la altura, así que se recomienda aclimatarse antes en Huaraz.",
            "latitud" to -9.0114,
            "longitud" to -77.6153,
            "incluye" to listOf("Transporte desde Huaraz", "Guía de montaña", "Desayuno", "Entrada al parque"),
            "mejorEpoca" to "Mayo a septiembre"
        ),
        "2" to mapOf(
            "galeria" to listOf("vinicunca_1", "vinicunca_2", "vinicunca_3"),
            "descripcion" to "Salida de madrugada desde Cusco para llegar temprano a la Montaña de Siete Colores, a más de 5.000 m de altura. Sus franjas de colores se deben a los minerales del suelo. Desde la cima se ve el nevado Ausangate. La subida final es corta pero intensa por la falta de oxígeno.",
            "latitud" to -13.8695,
            "longitud" to -71.3030,
            "incluye" to listOf("Transporte desde Cusco", "Desayuno y almuerzo", "Guía", "Entrada"),
            "mejorEpoca" to "Abril a noviembre"
        ),
        "3" to mapOf(
            "galeria" to listOf("machu_picchu_1", "machu_picchu_2", "machu_picchu_3"),
            "descripcion" to "Visita guiada a la ciudadela inca del siglo XV, una de las Nuevas Siete Maravillas del Mundo. Se llega en tren hasta Aguas Calientes y luego en bus hasta la entrada. El recorrido incluye el Templo del Sol, la plaza principal y los andenes agrícolas con vista al Huayna Picchu.",
            "latitud" to -13.1631,
            "longitud" to -72.5450,
            "incluye" to listOf("Tren ida y vuelta", "Bus a la ciudadela", "Entrada", "Guía"),
            "mejorEpoca" to "Abril a octubre"
        ),
        "4" to mapOf(
            "galeria" to listOf("valle_sagrado_1", "valle_sagrado_2", "valle_sagrado_3"),
            "descripcion" to "Recorrido de un día por el Valle Sagrado de los Incas, a orillas del río Urubamba. Incluye el mercado y el complejo arqueológico de Pisac, el pueblo inca de Ollantaytambo con su fortaleza y el pueblo textil de Chinchero.",
            "latitud" to -13.3048,
            "longitud" to -72.1160,
            "incluye" to listOf("Transporte", "Guía", "Almuerzo buffet"),
            "mejorEpoca" to "Abril a octubre"
        ),
        "5" to mapOf(
            "galeria" to listOf("cusco_esencial_1", "cusco_esencial_2", "cusco_esencial_3"),
            "descripcion" to "Dos días para conocer lo esencial de la antigua capital inca: la Plaza de Armas, la Catedral, el templo del Qorikancha, el barrio bohemio de San Blas y los complejos arqueológicos de Sacsayhuamán, Q'enqo y Tambomachay.",
            "latitud" to -13.5165,
            "longitud" to -71.9786,
            "incluye" to listOf("Alojamiento 1 noche", "Traslados", "Guía", "Entradas"),
            "mejorEpoca" to "Abril a octubre"
        ),
        "6" to mapOf(
            "galeria" to listOf("canon_colca_1", "canon_colca_2", "canon_colca_3"),
            "descripcion" to "Viaje desde Arequipa hacia uno de los cañones más profundos del mundo. Se pasa por la Reserva de Salinas y Aguada Blanca, donde se ven vicuñas, y se pernocta en Chivay con tiempo para los baños termales. A la mañana siguiente se visita el mirador Cruz del Cóndor para ver volar a los cóndores andinos.",
            "latitud" to -15.6117,
            "longitud" to -71.9067,
            "incluye" to listOf("Transporte desde Arequipa", "Alojamiento en Chivay", "Desayunos", "Guía"),
            "mejorEpoca" to "Mayo a noviembre"
        ),
        "7" to mapOf(
            "galeria" to listOf("islas_ballestas_1", "islas_ballestas_2", "islas_ballestas_3"),
            "descripcion" to "Paseo en lancha desde Paracas hasta las Islas Ballestas, conocidas como las \"Galápagos peruanas\". En el camino se ve el geoglifo del Candelabro, y en las islas habitan lobos marinos, pingüinos de Humboldt y miles de aves guaneras.",
            "latitud" to -13.7369,
            "longitud" to -76.3966,
            "incluye" to listOf("Paseo en lancha", "Guía", "Chaleco salvavidas"),
            "mejorEpoca" to "Todo el año (mejor por la mañana)"
        ),
        "8" to mapOf(
            "galeria" to listOf("ruta_pisco_1", "ruta_pisco_2", "ruta_pisco_3"),
            "descripcion" to "Recorrido por bodegas tradicionales y artesanales de Ica, cuna del pisco. Se conoce el proceso de elaboración, desde el pisado de la uva hasta la destilación en falcas, y se termina con una degustación de piscos y vinos.",
            "latitud" to -14.0755,
            "longitud" to -75.7342,
            "incluye" to listOf("Transporte entre bodegas", "Guía", "Degustación"),
            "mejorEpoca" to "Marzo (Festival de la Vendimia)"
        ),
        "9" to mapOf(
            "galeria" to listOf("lunahuana_1", "lunahuana_2", "lunahuana_3"),
            "descripcion" to "Día de aventura en el valle de Lunahuaná: canotaje en el río Cañete, canopy sobre el valle y visita a bodegas de vino y pisco. Se cierra con un almuerzo de camarones, el plato típico de la zona.",
            "latitud" to -12.9603,
            "longitud" to -76.1417,
            "incluye" to listOf("Transporte desde Lima", "Canotaje con equipo", "Guía", "Almuerzo"),
            "mejorEpoca" to "Diciembre a abril (río más caudaloso)"
        ),
        "10" to mapOf(
            "galeria" to listOf("pachacamac_1", "pachacamac_2", "pachacamac_3"),
            "descripcion" to "Visita al santuario arqueológico de Pachacamac, uno de los centros religiosos más importantes del antiguo Perú, usado durante más de mil años por distintas culturas y luego por los incas. Incluye el Templo del Sol, el Templo Pintado y el museo de sitio.",
            "latitud" to -12.2567,
            "longitud" to -76.9006,
            "incluye" to listOf("Entrada", "Guía", "Museo de sitio"),
            "mejorEpoca" to "Todo el año"
        ),
        "11" to mapOf(
            "galeria" to listOf("laguna_churup_1", "laguna_churup_2", "laguna_churup_3"),
            "descripcion" to "Caminata desde Pitec hasta la laguna Churup, a unos 4.450 m, al pie del nevado del mismo nombre. Es una buena ruta de aclimatación antes de la Laguna 69. El último tramo es rocoso y tiene pasos con cuerdas fijas.",
            "latitud" to -9.4783,
            "longitud" to -77.4250,
            "incluye" to listOf("Transporte desde Huaraz", "Guía", "Box lunch"),
            "mejorEpoca" to "Mayo a septiembre"
        ),
        "12" to mapOf(
            "galeria" to listOf("norte_chico_1", "norte_chico_2", "norte_chico_3"),
            "descripcion" to "Escapada de un día a las playas tranquilas del norte de Lima, lejos de las multitudes del sur. Hay tiempo libre para bañarse, caminar por la orilla y almorzar pescados y mariscos frescos en un restaurante local.",
            "latitud" to -11.4950,
            "longitud" to -77.2070,
            "incluye" to listOf("Transporte desde Lima", "Almuerzo marino"),
            "mejorEpoca" to "Diciembre a marzo (verano)"
        ),
        "13" to mapOf(
            "galeria" to listOf("pesca_artesanal_1", "pesca_artesanal_2", "pesca_artesanal_3"),
            "descripcion" to "Experiencia vivencial con pescadores artesanales del Callao: se sale en bote al amanecer, se aprenden las técnicas tradicionales de pesca y luego se prepara un ceviche con lo pescado. Termina con un paseo por La Punta.",
            "latitud" to -12.0717,
            "longitud" to -77.1622,
            "incluye" to listOf("Salida en bote", "Equipo de pesca", "Ceviche", "Chaleco salvavidas"),
            "mejorEpoca" to "Todo el año"
        ),
        "14" to mapOf(
            "galeria" to listOf("lima_nocturna_1", "lima_nocturna_2", "lima_nocturna_3"),
            "descripcion" to "Recorrido nocturno por Miraflores: el Malecón con vista al mar iluminado, el Parque del Amor, el Parque Kennedy y el centro comercial Larcomar, construido sobre el acantilado.",
            "latitud" to -12.1211,
            "longitud" to -77.0297,
            "incluye" to listOf("Guía", "Degustación de pisco sour"),
            "mejorEpoca" to "Todo el año"
        ),
        "15" to mapOf(
            "galeria" to listOf("feria_artesanal_1", "feria_artesanal_2", "feria_artesanal_3"),
            "descripcion" to "Paseo por las ferias y talleres artesanales de Barranco, el distrito bohemio de Lima. Hay textiles, cerámica y retablos de distintas regiones del país, y el recorrido pasa por el Puente de los Suspiros y la Bajada de Baños.",
            "latitud" to -12.1491,
            "longitud" to -77.0219,
            "incluye" to listOf("Guía"),
            "mejorEpoca" to "Todo el año (fines de semana)"
        )
    )
}
