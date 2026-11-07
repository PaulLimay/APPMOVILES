package com.example.ligortravel.data

// Siembra temporal: los paquetes turísticos iniciales. PaqueteRepository.sembrarSiEstaVacia los
// sube a Firestore la primera vez que se abre el Home con la colección "paquetes" vacía, y
// PaqueteRepository.completarDetallesSiFaltan agrega "descripcion" y "serviciosIncluidos" a los
// paquetes que se subieron antes de que existieran esos campos. Una vez que estén en Firestore,
// borrar este archivo, esas funciones y la llamada en HomeViewModel.
// experienciaIds apunta a los documentos "1".."15" de la colección "experiencias". Cada precio
// es menor que la suma de sus experiencias por separado (la pantalla muestra el ahorro).
object PaquetesIniciales {
    val lista = listOf(
        Paquete(
            id = "cusco_imperial",
            titulo = "Cusco Imperial",
            destino = "Cusco",
            duracion = "5 días / 4 noches",
            precio = 750, // por separado: 220 + 180 + 250 + 190 = 840
            imagen = "machu_picchu",
            experienciaIds = listOf("5", "4", "3", "2"),
            orden = 1,
            descripcion = "El recorrido completo por la capital inca y sus alrededores. Los primeros días se dedican a la ciudad y al Valle Sagrado para aclimatarse a la altura; luego se visita Machu Picchu y se cierra con la Montaña de Siete Colores, la caminata más exigente del viaje.",
            serviciosIncluidos = listOf(
                "Hotel 3★ en Cusco (4 noches)",
                "Desayunos diarios",
                "Traslados aeropuerto – hotel – aeropuerto",
                "Coordinador de viaje",
                "Asistencia 24/7"
            )
        ),
        Paquete(
            id = "huaraz_aventura",
            titulo = "Huaraz Aventura",
            destino = "Huaraz, Áncash",
            duracion = "3 días / 2 noches",
            precio = 199, // por separado: 80 + 150 = 230
            imagen = "laguna_69",
            experienciaIds = listOf("11", "1"),
            orden = 2,
            descripcion = "Dos caminatas a lagunas glaciares de la Cordillera Blanca en orden de dificultad: primero la laguna Churup para aclimatarse y, al día siguiente, la Laguna 69, a 4.600 m de altura.",
            serviciosIncluidos = listOf(
                "Hospedaje en Huaraz (2 noches)",
                "Desayunos",
                "Bastones de trekking",
                "Botiquín y oxígeno de emergencia"
            )
        ),
        Paquete(
            id = "costa_sur",
            titulo = "Costa Sur",
            destino = "Paracas e Ica",
            duracion = "2 días / 1 noche",
            precio = 115, // por separado: 60 + 70 = 130
            imagen = "islas_ballestas",
            experienciaIds = listOf("7", "8"),
            orden = 3,
            descripcion = "Fin de semana en la costa sur: la mañana en lancha por las Islas Ballestas para ver lobos marinos y pingüinos, y la tarde entre bodegas de pisco en Ica.",
            serviciosIncluidos = listOf(
                "Bus turístico Lima – Paracas – Lima",
                "Hotel en Paracas (1 noche)",
                "Desayuno",
                "Traslados entre Paracas e Ica"
            )
        ),
        Paquete(
            id = "arequipa_colca",
            titulo = "Arequipa y Colca",
            destino = "Arequipa",
            duracion = "2 días / 1 noche",
            precio = 185, // por separado: 200
            imagen = "canon_colca",
            experienciaIds = listOf("6"),
            orden = 4,
            descripcion = "Salida desde Arequipa hacia el Cañón del Colca, con noche en Chivay y baños termales. Al día siguiente, el vuelo de los cóndores en la Cruz del Cóndor y regreso a la ciudad blanca.",
            serviciosIncluidos = listOf(
                "Recojo del hotel en Arequipa",
                "Entrada a los baños termales",
                "Boleto turístico del Colca",
                "Asistencia 24/7"
            )
        ),
        Paquete(
            id = "lima_clasica",
            titulo = "Lima Clásica",
            destino = "Lima",
            duracion = "2 días",
            precio = 110, // por separado: 25 + 30 + 35 + 40 = 130
            imagen = "lima_nocturna",
            experienciaIds = listOf("10", "15", "13", "14"),
            orden = 5,
            descripcion = "Lima en dos días: historia prehispánica en Pachacamac, arte y artesanía en Barranco, la pesca artesanal del Callao y la vida nocturna de Miraflores frente al mar.",
            serviciosIncluidos = listOf(
                "Movilidad privada entre las actividades",
                "Recojo y retorno al hotel",
                "Guía local los dos días"
            )
        ),
        Paquete(
            id = "escapadas_lima",
            titulo = "Escapadas desde Lima",
            destino = "Alrededores de Lima",
            duracion = "2 días",
            precio = 125, // por separado: 90 + 50 = 140
            imagen = "lunahuana",
            experienciaIds = listOf("9", "12"),
            orden = 6,
            descripcion = "Dos escapadas de un día para salir de la ciudad: aventura y canotaje en el valle de Lunahuaná, y un día tranquilo en las playas del norte de Lima.",
            serviciosIncluidos = listOf(
                "Recojo y retorno a tu hotel en Lima",
                "Seguro de viaje",
                "Snacks y agua en el bus"
            )
        )
    )
}
