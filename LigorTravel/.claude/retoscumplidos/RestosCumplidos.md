# Retos de código — LigorTravel

Registro de los retos de `/reto-codigo`. Cada reto guarda el código **original** de lo que toca,
para poder revertirlo después y dejar la app como estaba antes del reto.

**Niveles:**
- **Nivel 1 — Puntual:** un solo cambio en una pantalla.
- **Nivel 2 — Atraviesa la app:** un solo cambio que pasa por varias partes
  (pantalla → modelo → donde se guardan los datos → donde se muestran).

## 🔄 Repaso (2026-10-18)

El 2026-10-18 se **revirtieron los 7 retos** para volver a resolverlos de repaso: el código quedó
exactamente como antes del Reto 1 (mismo `git diff --stat` que al inicio, compila y pasan los
tests). Se restauraron los archivos desde `originales/` (Retos 3 a 7) y los fragmentos de este
archivo (Retos 1 y 2), y se borró `ui/acercade/AcercaDe.kt`.

- Las soluciones del estudiante (versión 2026-10-17) quedaron guardadas en
  `.claude/retoscumplidos/soluciones_2026-10-17/`, para compararlas con las del repaso.
- En Firestore, los documentos de `usuarios` que guardaron una `ciudad` (Reto 3) conservan ese
  campo; la app lo ignora sin errores porque el modelo ya no lo tiene.
- Las reglas de Firestore no se habían cambiado en ningún reto, así que no hay nada que revertir ahí.

## 🔄 Segunda reversión (2026-10-20)

El 2026-10-20 se revirtieron **todos** los retos (repaso 1–7, Reto 8 y Reto 9): el código de `app/` volvió
exactamente al commit `28db33c` (`git restore`), se borró `ui/acercade/AcercaDeScreen.kt`; compila y pasan los
tests. Las soluciones de ese momento quedaron en `.claude/retoscumplidos/soluciones_2026-10-20/`.

- **Pendiente en la consola de Firebase:** las reglas publicadas de `reservas` todavía incluyen `nota` (Reto 8).
  Hay que volver a publicar la versión de referencia de `CLAUDE.md` (sin `nota`). El Reto 9 nunca llegó a
  cambiar las reglas.
- Datos en Firestore que quedan de los retos y la app ignora sin errores: `ciudad` en `usuarios` y `nota` /
  `opcion` en `reservas` creadas durante las pruebas.
- Pendiente de seguridad (no es de los retos): cerrar `allow write` de `experiencias` y `paquetes` con `if false`.

## Índice

| # | Reto | Nivel | Tipo | Estado |
|---|------|-------|------|--------|
| 1 | Validación del nombre en el registro | Nivel 1 | Validaciones | ✅ Cumplido · ✅ Repaso cumplido · 🔄 Revertido |
| 2 | Precios con 2 decimales en la lista de paquetes | Nivel 1 | Presentación de datos | ✅ Cumplido · ✅ Repaso cumplido · 🔄 Revertido |
| 3 | Campo "Ciudad" en el perfil | Nivel 2 | Campo nuevo de punta a punta | ✅ Cumplido · ✅ Repaso cumplido · 🔄 Revertido |
| 4 | Botón "Reintentar" en Mis reservas | Nivel 1 | Estados de carga y error | ✅ Cumplido · ✅ Repaso cumplido · 🔄 Revertido |
| 5 | De una reserva al detalle de su paquete | Nivel 2 | Navegación | ✅ Cumplido · ✅ Repaso cumplido · 🔄 Revertido |
| 6 | Pantalla "Acerca de" desde el Perfil | Nivel 1 | Navegación | ✅ Cumplido · ✅ Repaso cumplido · 🔄 Revertido |
| 7 | Confirmar antes de cerrar sesión | Nivel 1 | Retroalimentación al usuario | ✅ Cumplido · ✅ Repaso cumplido · 🔄 Revertido |
| 8 | Nota opcional en la reserva | Nivel 2 | Campo nuevo de punta a punta | ✅ Cumplido (con guía) · 🔄 Revertido |
| 9 | Traslado desde el hotel en la reserva | Nivel 2 | Campo nuevo de punta a punta | ⏸️ Incompleto (faltaban las reglas) · 🔄 Revertido |
| 10 | Recuperar contraseña ("¿Olvidaste?") | Nivel 2 | Navegación + nueva acción con Firebase | 🟡 En curso |
| 11 | Pantalla vacía desde "Favoritos" | Nivel 1 | Navegación | ✅ Cumplido (1 revisión) · 🔄 Revertido |
| 12 | Título y mensaje en Favoritos | Nivel 1 | Navegación (contenido de la pantalla) | ✅ Cumplido (1 revisión) · 🔄 Revertido |
| 13 | Botón para volver en Favoritos | Nivel 1 | Navegación | ✅ Cumplido (2 revisiones, con fragmento) · 🔄 Revertido |
| 14 | De Favoritos al mapa | Nivel 2 | Navegación (pantalla nueva → pantalla existente) | ✅ Cumplido (1 revisión) · 🔄 Revertido |
| 15 | Pantalla Notificaciones desde la campana | Nivel 1 | Navegación (pantalla nueva con texto) | ✅ Cumplido al primer intento · 🔄 Revertido |

---

## Reto 1 — Validación del nombre en el registro

- **Nivel:** Nivel 1 — Puntual
  - **Tipo:** Validaciones
- **Fecha:** 2026-10-16
- **Estado:** ✅ Cumplido (2026-10-16, tras 1 corrección)
- **Pedido:** En la pestaña Registro, el nombre debe tener al menos 3 caracteres y solo letras y
  espacios (aceptando tildes y ñ, ej. "José Ñáñez"). Si no cumple, no se crea la cuenta y se
  muestra un mensaje.
- **Punto de partida:** commit `4f84455` + cambios sin commitear que **no** son del reto
  (pantallas de detalle, paquetes, reservas, mis reservas, siembra temporal, fotos de galería).
- **Código original a restaurar** (`app/src/main/java/com/example/ligortravel/ui/registro/RegistroViewModel.kt`,
  función `validar`):

```kotlin
    private fun validar(estado: RegistroUiState): String? = when {
        estado.nombre.isBlank() -> "El nombre es obligatorio"
        !emailRegex.matches(estado.email.trim()) -> "Ingresa un correo válido"
        estado.telefono.trim().let { tel ->
            tel.length !=9 || !tel.startsWith("9") || !tel.all{
                it.isDigit()} } -> "El teléfono debe empezar con 9"
        estado.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        estado.password != estado.confirmarPassword -> "Las contraseñas no coinciden"
        else -> null
    }
```

- **Archivos que el reto podría tocar:** `ui/registro/RegistroViewModel.kt` y quizás
  `ui/auth/AuthScreen.kt` (formulario de registro). Al revertir, comparar contra este punto de
  partida y deshacer solo lo del reto.
- **Solución del estudiante** (reemplaza la primera línea del `when` en `validar`):

```kotlin
    private fun validar(estado: RegistroUiState): String? =
        when {
        estado.nombre.isBlank() -> "El nombre no debe estar en blanco"
        estado.nombre.count{it.isLetter()} < 3 -> "Debe contener al menos 3 caracteres"
        !estado.nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+$")) -> "El nombre debe contener solo letras y espacios"
```

- **Qué practicó:** expresiones regulares con caracteres en español, reglas separadas en un
  `when` con un mensaje por cada regla, y validar el mismo texto que se guarda (los espacios no
  cuentan como caracteres).
- **Conceptos utilizados:**
  - **Expresión regular (`Regex`) y `matches()`:** un patrón que describe qué texto es válido.
    `^...$` obliga a que el texto **completo** cumpla el patrón (no solo una parte), `[...]` es
    el conjunto de caracteres permitidos y `+` significa "uno o más". Las tildes, la ñ y la ü se
    listan a mano porque `a-zA-Z` solo cubre el alfabeto inglés.
  - **`when` sin argumento:** evalúa las condiciones de arriba hacia abajo y se queda con la
    **primera** que sea verdadera. Por eso el orden importa: primero "vacío", luego "muy corto" y
    después "caracteres inválidos", para que el mensaje sea el más útil.
  - **`when` como expresión (`= when { ... }`):** la función devuelve directamente el valor de la
    rama elegida: un `String` con el error, o `null` si todo está bien.
  - **Tipo nullable (`String?`):** `validar()` devuelve `null` para decir "no hay error". Así
    `registrar()` sabe si debe detenerse o seguir con Firebase.
  - **`isBlank()`:** es verdadero si el texto está vacío o tiene solo espacios (a diferencia de
    `isEmpty()`, que solo detecta el texto vacío).
  - **`count { it.isLetter() }`:** función de orden superior con una lambda; cuenta los caracteres
    que cumplen la condición. `it` es cada carácter, e `isLetter()` también reconoce letras con
    tilde. Así los espacios no cuentan para el mínimo.
  - **Validar lo mismo que se guarda:** el perfil se guarda con `trim()`, así que la regla del
    mínimo no debe dejarse engañar por los espacios de los extremos.
  - **Validación en el ViewModel (MVVM):** la regla vive en el `ViewModel` y no en la pantalla.
    La pantalla solo muestra `state.error`, y `onNombreChange` limpia el error cuando el usuario
    corrige el campo.
  - **Validar antes de llamar al servidor:** si el nombre es inválido, `registrar()` hace
    `return` antes de crear la cuenta en Firebase Auth, así que no se crea ninguna cuenta a medias.


- **Repaso (2026-10-18):** ✅ Cumplido **al primer intento**, sin correcciones. Agregó dos líneas al
  `when` de `validar` (sin tocar la primera ni el formato de la función):

  ```kotlin
  estado.nombre.count{it.isLetter()} < 3 -> "El nombre debe contener al menos 3 caracteres"
  !estado.nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+$")) -> "EL nombre debe contener solo letras y espacios"
  ```

  Comparado con la primera vez (1 corrección): ya aplicó desde el inicio contar solo letras y un mensaje
  por regla; además conservó el mensaje original "El nombre es obligatorio" y el estilo `= when {`.
  Detalle menor: "EL" con L mayúscula en el último mensaje.

---

## Reto 2 — Precios con 2 decimales en la lista de paquetes

- **Nivel:** Nivel 1 — Puntual
- **Tipo:** Presentación de datos
- **Fecha:** 2026-10-16
- **Estado:** ✅ Cumplido (2026-10-16, tras 3 correcciones)
- **Pedido:** En la pantalla de Paquetes, el precio de cada tarjeta y la etiqueta "Ahorras"
  deben mostrarse siempre con 2 decimales y punto decimal (ej. `S/ 750` → `S/ 750.00`,
  `Ahorras S/ 90` → `Ahorras S/ 90.00`).
- **Punto de partida:** el del Reto 1 + la solución del Reto 1 ya aplicada en `RegistroViewModel.kt`.
- **Código original a restaurar** (`app/src/main/java/com/example/ligortravel/ui/paquetes/PaquetesScreen.kt`,
  función `PiePrecio`; las dos líneas que muestran precios):

```kotlin
                Text(
                    text = "Ahorras S/ $ahorro",
```

```kotlin
            Text(
                text = "S/ $precio",
```

- **Archivos que el reto podría tocar:** `ui/paquetes/PaquetesScreen.kt` (y quizás un archivo
  nuevo con una función de formato). Al revertir, deshacer solo lo del reto.
- **Solución del estudiante** (además agregó `import java.util.Locale` y dejó comentadas las
  líneas originales):

```kotlin
                    text = "Ahorras S/ %.2f".format(Locale.US,ahorro.toDouble()),
```

```kotlin
                text = "S/ %.2f".format(Locale.US, precio.toDouble()),
```

- **Qué practicó:** formatear números para mostrarlos, sin cambiar el tipo del dato; leer
  errores de compilación; y distinguir un error de compilación de uno de ejecución.
- **Conceptos utilizados:**
  - **`String.format` y los especificadores de formato:** `%.2f` es un hueco que se llena con un
    número decimal (`f` = *floating point*) redondeado a 2 decimales. Cada `%` del patrón consume
    un argumento, en orden, y el texto fijo ("Ahorras S/ ") puede ir dentro del propio patrón.
  - **`Locale`:** define las convenciones regionales. Sin indicarlo, `format` usa el idioma del
    teléfono, y en algunos idiomas el separador decimal es la coma (`90,00`). Con `Locale.US`
    siempre sale el punto (`90.00`).
  - **Tipado estricto de Kotlin:** Kotlin no convierte solo un `Int` en `Double`
    ("Argument type mismatch"); la conversión es explícita, con `.toDouble()`, `.toInt()`, etc.
  - **Separar el dato de su presentación:** el precio sigue siendo `Int` en el modelo y en
    Firestore. Solo cambia cómo se muestra, justo al construir el texto. Cambiar el tipo del
    parámetro rompía a quien llamaba a la función.
  - **Error de compilación vs. de ejecución:** pasar dos veces `text =` o un `Double` donde se pide
    `Int` lo detecta el compilador. En cambio, `format` revisa los tipos recién al ejecutarse:
    `"%.2f".format(unInt)` compila, pero lanza `IllegalFormatConversionException` y cierra la app.
  - **Parámetros con nombre:** en `Text(text = ..., color = ...)` cada parámetro puede aparecer
    una sola vez; duplicarlo provoca "None of the following candidates is applicable".

- **Repaso (2026-10-18):** ✅ Cumplido **al primer intento**, sin correcciones. Mismo resultado que la
  solución final de la primera vez, pero sin dejar las líneas originales comentadas:

  ```kotlin
  text = "Ahorras S/ %.2f".format(Locale.US,ahorro.toDouble()),
  text = "S/ %.2f".format(Locale.US,precio.toDouble()),
  ```

  Comparado con la primera vez (3 correcciones: cambiar el tipo a `Double`, `text` duplicado y `%.2f`
  con un `Int`): esta vez dejó el dato como `Int` y convirtió con `.toDouble()` solo al formatear, usó
  `Locale.US` desde el inicio y cambió los dos precios.

---

## Reto 3 — Campo "Ciudad" en el perfil

- **Nivel:** Nivel 2 — Atraviesa la app
- **Tipo:** Campo nuevo de punta a punta
- **Fecha:** 2026-10-16
- **Estado:** ✅ Cumplido (2026-10-16, tras 3 correcciones; probado en la app por el estudiante)
- **Pedido:** En el Perfil, agregar el dato "Ciudad": se ve junto a los demás datos, se puede
  editar con "Editar" y "Guardar cambios", y se sigue viendo después de cerrar sesión y volver a
  entrar. Los usuarios que ya existían (sin ciudad) deben seguir pudiendo entrar sin errores. Si
  las reglas de la base de datos impiden guardarlo, también hay que ajustarlas.
- **Punto de partida:** el del Reto 2 + las soluciones de los Retos 1 y 2 ya aplicadas.
- **Código original a restaurar:** como el reto toca varios archivos, se guardó una **copia
  completa** de cada archivo que podría tocar en `.claude/retoscumplidos/originales/reto3/`
  (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  `data/RegistroUsuario.kt`, `data/UserRepository.kt`, `ui/perfil/PerfilViewModel.kt`,
  `ui/perfil/PerfilScreen.kt`, `ui/registro/RegistroViewModel.kt`, `ui/auth/AuthScreen.kt` y
  `ui/login/LoginViewModel.kt`. Al revertir, comparar contra esas copias (que ya incluyen los
  Retos 1 y 2) y, si cambian las reglas de Firestore en la consola, volver a dejarlas como estaban.
- **Reglas de Firestore:** no hubo que cambiarlas. Las de `usuarios` solo exigen que cada usuario
  escriba su propio documento y no limitan los campos.
- **Solución del estudiante** (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  - `data/RegistroUsuario.kt`: `val ciudad: String = ""` en el modelo.
  - `ui/perfil/PerfilViewModel.kt`: `ciudad` y `ciudadOriginal` en `PerfilUiState`;
    `ciudad = usuario.ciudad` en `cargarUsuarioActual()`; `onCiudadChange()`;
    `ciudadOriginal = ...ciudad` en `activarEdicion()`; `ciudad = ...ciudadOriginal` en
    `cancelarEdicion()`; `ciudad = estado.ciudad.trim()` en `guardarCambios()`.
  - `ui/perfil/PerfilScreen.kt`: `CampoCuenta` "CIUDAD" con `viewModel::onCiudadChange`,
    `habilitado = uiState.editando` e `Icons.Filled.Apartment` (más su `import`).
- **Qué practicó:** seguir un dato por todas las capas de MVVM (modelo → estado → pantalla →
  repositorio → sesión), copiando el patrón de un campo que ya existía, y probar de punta a punta
  en la app.
- **Conceptos utilizados:**
  - **Flujo de un dato en MVVM:** un campo nuevo debe existir en cada capa por la que pasa:
    modelo de datos (`RegistroUsuario`), estado de la pantalla (`PerfilUiState`), eventos
    (`onCiudadChange`), la vista (`CampoCuenta`) y la acción que lo persiste (`guardarCambios`).
    Si falta en una sola capa, el dato "se pierde" en el camino.
  - **Valores por defecto y compatibilidad hacia atrás:** `ciudad: String = ""` hace que Firestore
    pueda leer documentos antiguos sin ese campo (`toObject` usa el valor por defecto). Por eso los
    usuarios que ya existían no rompen la app.
  - **`set()` reemplaza el documento entero:** el repositorio guarda con `.set(usuario)`, así que
    cualquier campo que no se incluya en el objeto se sobrescribe con su valor por defecto. Por eso
    era obligatorio pasar `ciudad` al construir `usuarioActualizado` (la alternativa sería
    `update()` con solo los campos cambiados).
  - **Parámetros con valor por defecto vs. obligatorios:** `CampoCuenta` exige `icono` (no tiene
    valor por defecto) y por eso falló la compilación con "No value passed for parameter 'icono'".
    En cambio, `tipoTeclado` es opcional.
  - **Referencia a función (`viewModel::onCiudadChange`):** se pasa la función del ViewModel como
    callback del campo en vez de una lambda vacía `{}`, que descartaría lo que escribe el usuario.
  - **Copia del valor original para "Cancelar":** se guarda el valor al entrar en edición
    (`ciudadOriginal = ciudad`) y se restaura al cancelar (`ciudad = ciudadOriginal`). La dirección
    de la asignación importa: invertida, pisa la copia en vez de restaurar.
  - **Estado inmutable con `copy()`:** cada cambio crea un nuevo `PerfilUiState` con `copy(...)`,
    y el `StateFlow` avisa a la pantalla para que se redibuje.
  - **Sesión en memoria:** tras guardar, `SessionManager.iniciarSesion(usuario)` actualiza al
    usuario actual; al volver a entrar, el login lee el perfil de Firestore, por lo que la ciudad
    reaparece.
  - **Reglas de seguridad:** antes de agregar un campo hay que revisar si las reglas limitan las
    claves (ej. `keys().hasOnly([...])`). Las de `usuarios` no lo hacen; las de `reservas` sí.

- **Repaso (2026-10-18):** ✅ Cumplido **tras 1 revisión** (la primera vez fueron 3 correcciones) y probado
  en la app. Mismos cambios que la solución original: `ciudad = ""` en `RegistroUsuario`; `ciudad` y
  `ciudadOriginal` en `PerfilUiState`; `ciudad = usuario.ciudad` en `cargarUsuarioActual()`;
  `onCiudadChange()`; copia en `activarEdicion()` y restauración en `cancelarEdicion()`;
  `ciudad = estado.ciudad.trim()` en `guardarCambios()`; y `CampoCuenta` "CIUDAD" editable con
  `Icons.Filled.Apartment`.

  Comparado con la primera vez: no le faltó el ícono, conectó el campo como editable desde el inicio y
  restauró `ciudad = ciudadOriginal` en la dirección correcta (antes la tenía invertida). Lo único
  pendiente en la primera revisión fue incluir la ciudad al guardar (la trampa de `set()`). Además separó
  cada asignación de `activarEdicion()` / `cancelarEdicion()` en su propia línea, más legible.

---

## Reto 4 — Botón "Reintentar" en Mis reservas

- **Nivel:** Nivel 1 — Puntual
- **Tipo:** Estados de carga y error
- **Fecha:** 2026-10-16
- **Estado:** ✅ Cumplido (2026-10-16, tras 3 correcciones; probado en la app por el estudiante)
- **Pedido:** En Mis reservas, cuando falla la carga y aparece el mensaje de error, mostrar debajo
  un botón "Reintentar". Al tocarlo debe volver a aparecer el indicador de carga y la app debe
  intentar cargar las reservas de nuevo.
- **Punto de partida:** el del Reto 3 + las soluciones de los Retos 1, 2 y 3 ya aplicadas.
- **Código original a restaurar:** copia completa de los archivos que podría tocar en
  `.claude/retoscumplidos/originales/reto4/` (rutas relativas a
  `app/src/main/java/com/example/ligortravel/`): `ui/misreservas/MisReservasScreen.kt`,
  `ui/misreservas/MisReservasViewModel.kt` y `data/ReservaRepository.kt`.
- **Solución del estudiante** (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  - `ui/misreservas/MisReservasViewModel.kt`: `import kotlinx.coroutines.Job`;
    `private var trabajoCarga: Job? = null` declarado **antes** del `init`;
    `trabajoCarga?.cancel()` al inicio de `observarReservas()`;
    `trabajoCarga = viewModelScope.launch { ... }`; y la función pública:

    ```kotlin
    fun reintentarReservas(){
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        observarReservas()
    }
    ```
  - `ui/misreservas/MisReservasScreen.kt`: en la rama `uiState.error != null`, debajo del mensaje:

    ```kotlin
    Button(
        onClick = {
            viewModel.reintentarReservas()
        }) {
        Text("Reintentar")
    }
    ```
- **Cómo se probó:** con código temporal (`intento` + `"uid_falso"` en el primer intento), para
  que las reglas rechazaran la primera carga y el reintento usara el uid real. Ya fue borrado.
- **Qué practicó:** manejar los estados cargando / error / éxito, el patrón "la pantalla avisa y el
  ViewModel actúa", la gestión de corrutinas con `Job`, y cómo provocar un error a propósito
  para probar.
- **Conceptos utilizados:**
  - **Estados de una pantalla con datos remotos:** el `when` de la pantalla elige qué mostrar
    según `cargando` / `error` / datos. Reintentar es volver al estado "cargando"
    (`cargando = true, error = null`) y repetir la carga.
  - **Eventos de UI en MVVM:** la pantalla no recarga datos; el `onClick` solo llama a una función
    pública del ViewModel (`reintentarReservas()`), que decide qué hacer.
  - **`@Composable` vs. código normal:** un composable describe qué se dibuja y solo puede
    llamarse desde otro composable. Un `onClick` es una lambda normal, por eso
    `onClick = { ListaReservas() }` no compilaba.
  - **`LazyColumn` y su contenido:** lo que va dentro de una lista se dibuja solo cuando la carga
    fue exitosa. El lugar de un elemento en el árbol de la UI decide cuándo aparece.
  - **`Job` y cancelación de corrutinas:** `launch` devuelve un `Job`. Guardarlo y cancelarlo
    antes de lanzar otro evita acumular escuchas de Firestore (cada reintento abriría una nueva y
    las viejas quedarían suspendidas). `cancel()` termina el `collect` y, con él, `awaitClose`
    quita el listener (`registro.remove()`).
  - **Llamada segura `?.`:** `trabajoCarga?.cancel()` no hace nada si es `null`. Por eso, sin asignar
    el `Job`, compilaba pero no cancelaba nada: un fallo silencioso.
  - **Orden de inicialización en Kotlin:** las propiedades y los bloques `init` se ejecutan en el
    orden en que están escritos. Una propiedad declarada después del `init` (`= null` / `= 0`)
    pisa lo que el `init` hizo con ella. Además, se detectó que el mismo bug existe en
    `PaquetesViewModel.kt` (`trabajoCarga` declarado después del `init`); quedó **sin corregir**,
    fuera del reto.
  - **`callbackFlow` ante errores:** el repositorio envía `Result.failure` pero no cierra el flujo,
    así que el `collect` viejo seguía vivo; esto justifica cancelar el `Job`.
  - **Probar errores a propósito:** con el modo avión Firestore puede responder desde la caché sin
    error. Se forzó un `PERMISSION_DENIED` consultando con otro uid, aprovechando que las reglas
    rechazan consultas que podrían devolver documentos ajenos. Después se borró el código temporal.

- **Repaso (2026-10-18):** ✅ Cumplido **tras 3 revisiones** (igual que la primera vez) y probado en la
  app con el código temporal del uid falso (ya borrado). Solución final igual a la original: botón
  "Reintentar" dentro de la rama `uiState.error != null`, `reintentarReservas()` en el ViewModel y
  `private var trabajoCarga: Job?` declarado antes del `init`, con `cancel()` + asignación del `launch`.

  Errores del repaso: (1) el `Button` quedó entre dos ramas del `when` (Kotlin lo leyó como condición:
  "Condition type mismatch: Unit / Boolean"); (2) marcó la función del ViewModel con `@Composable`
  (y con mayúscula inicial); (3) declaró `trabajoCarga` como `val` y sin asignar el `launch` (el mismo
  fallo silencioso de `?.` que la primera vez). Mejora respecto a la primera vez: declaró el `Job`
  **antes del `init` desde el principio** y no intentó llamar a un composable desde el `onClick`.
  Conceptos reforzados: `val` vs `var`, convención de nombres (minúscula = función normal, mayúscula =
  composable) y estructura `condición -> resultado` de las ramas de `when`.

---

## Reto 5 — De una reserva al detalle de su paquete

- **Nivel:** Nivel 2 — Atraviesa la app
- **Tipo:** Navegación (con argumento)
- **Fecha:** 2026-10-16
- **Estado:** ✅ Cumplido (2026-10-16, tras 5 correcciones; probado en la app por el estudiante)
- **Pedido:** En Mis reservas, al tocar una reserva (en cualquiera de las dos pestañas) se abre el
  detalle del paquete que se reservó. Con "atrás" (botón o gesto) se vuelve a Mis reservas, en la
  misma pestaña.
- **Punto de partida:** el del Reto 4 + las soluciones de los Retos 1 a 4 ya aplicadas.
- **Código original a restaurar:** copia completa de los archivos que podría tocar en
  `.claude/retoscumplidos/originales/reto5/` (rutas relativas a
  `app/src/main/java/com/example/ligortravel/`): `ui/misreservas/MisReservasScreen.kt`,
  `ui/misreservas/MisReservasViewModel.kt` y `ui/navigation/LigorNavGraph.kt`.
- **Solución del estudiante** (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  - `ui/misreservas/MisReservasScreen.kt`:
    - parámetro nuevo `onAbrirPaquete: (String) -> Unit` en `MisReservasScreen`, pasado a
      `ListaReservas`;
    - `ListaReservas(..., onAbrirPaquete: (String) -> Unit)` llama a
      `TarjetaReserva(item, esProximo, hoy, onClick = {onAbrirPaquete(item.reserva.paqueteId)})`;
    - `TarjetaReserva(..., onClick: () -> Unit)` con `.clickable(onClick = onClick)` antes del
      `.padding(14.dp)`, más `import androidx.compose.foundation.clickable`.
  - `ui/navigation/LigorNavGraph.kt`, en `composable(Rutas.MIS_RESERVAS)`:

    ```kotlin
    onAbrirPaquete = {id -> navController.navigate(Rutas.detallePaquete(id))}
    ```
- **Qué practicó:** hacer viajar un dato hacia arriba con callbacks por cada nivel de composables,
  navegar con argumento desde el NavGraph, y adaptar un patrón existente en vez de copiarlo.
- **Conceptos utilizados:**
  - **Navegación con argumento:** la ruta `detalle_paquete/{paqueteId}` tiene un "hueco" que se
    rellena al navegar. `Rutas.detallePaquete(id)` arma la ruta concreta (con `Uri.encode`) y el
    destino recibe el id como `NavType.StringType`.
  - **Elevación de eventos (state hoisting / callbacks hacia arriba):** la tarjeta no sabe
    navegar; solo avisa con `onClick`. Cada nivel (`TarjetaReserva` → `ListaReservas` →
    `MisReservasScreen` → `LigorNavGraph`) recibe un callback y lo pasa al de arriba. Solo el
    NavGraph, dueño del `navController`, decide a dónde ir.
  - **Por qué no usar `navController` dentro de la pantalla:** solo existe en `LigorNavGraph`, y
    `rememberNavController()` crearía otro controlador nuevo y vacío, que no navega en la app.
  - **Tipos de función y lambdas con parámetro:** `(String) -> Unit` recibe un `String`. La lambda
    lo nombra con `{ id -> ... }` (o usa `it`). Sin declararlo, `id` se resolvió a otro `id` de tipo
    `Int` que había en el contexto (**sombreado de nombres**): por eso el error decía "actual type is
    'Int'".
  - **Alcance de los parámetros:** cada función solo ve sus propios parámetros. `onVerPaquetes` no
    existía dentro de `ListaReservas` hasta pasárselo.
  - **Elegir el dato correcto del modelo:** `item.reserva.id` es el id de la reserva;
    `item.reserva.paqueteId` es el del paquete. El id equivocado compila, pero lleva a "no existe".
    `ReservaConFecha` solo tiene `reserva` y `fecha`.
  - **Orden de los modificadores en Compose:** cada modificador se aplica sobre el anterior.
    `.clickable` antes de `.padding` hace tocable toda la tarjeta (con ripple recortado por el
    `.clip`); después, solo el área interior.
  - **`items` solo dentro de `LazyColumn` y `else` al final de un `when`:** los primeros errores
    venían de pegar código fuera de su contexto.
  - **Reutilizar en vez de duplicar:** adaptar la `TarjetaReserva` existente en lugar de crear una
    tarjeta nueva copiada de Paquetes, con un tipo de dato (`PaqueteResumen`) de otra pantalla.
  - **Imports sobrantes:** Android Studio puede agregar imports que no se usan; se limpian con
    Code → Optimize Imports (Ctrl+Alt+O).

- **Repaso (2026-10-18/19):** ✅ Cumplido **tras 2 revisiones** (la primera vez fueron 5 correcciones).
  El estudiante pasó al siguiente reto sin confirmar explícitamente la prueba en la app.
  Solución final igual a la original, con el callback renombrado a `onAbrirPaquete: (paqueteId: String) -> Unit`
  en `MisReservasScreen` y `ListaReservas`, `onClick = {onAbrirPaquete(item.reserva.paqueteId)}` en el
  `items(...)`, `.clickable(onClick = onClick)` antes del `.padding` en `TarjetaReserva` y
  `onAbrirPaquete = {id -> navController.navigate(Rutas.detallePaquete(id))}` en el NavGraph.

  Comparado con la primera vez: no copió piezas de Paquetes, no usó `navController` dentro de la
  pantalla, puso el `.clickable` en el orden correcto y declaró el parámetro de la lambda (`id ->`)
  desde el inicio. Errores del repaso: una lambda que solo mencionaba un valor (`{ item.reserva.id }`)
  en vez de llamar al callback, y de nuevo el id equivocado (`reserva.id` en vez de `paqueteId`), el
  único error que se repitió de la primera vez.

---

## Reto 6 — Pantalla "Acerca de" desde el Perfil

- **Nivel:** Nivel 1 — Puntual
- **Tipo:** Navegación (pantalla nueva)
- **Fecha:** 2026-10-17
- **Estado:** ✅ Cumplido (2026-10-17, tras 3 revisiones)
- **Pedido:** En el Perfil, agregar un botón "Acerca de" que abra una pantalla nueva con el título
  "Acerca de LigorTravel" y el texto "Versión 1.0". La pantalla nueva debe tener un botón para
  volver al Perfil (y el gesto de "atrás" también debe funcionar).
- **Punto de partida:** el del Reto 5 + las soluciones de los Retos 1 a 5 ya aplicadas.
- **Código original a restaurar:** copia completa de los archivos que podría tocar en
  `.claude/retoscumplidos/originales/reto6/` (rutas relativas a
  `app/src/main/java/com/example/ligortravel/`): `ui/perfil/PerfilScreen.kt` y
  `ui/navigation/LigorNavGraph.kt`. Además, `lista_archivos_kt.txt` tiene la lista de archivos `.kt`
  que existían antes del reto: al revertir, **borrar** los archivos nuevos que haya creado (la
  pantalla nueva).
- **Solución del estudiante** (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  - **Archivo nuevo** `ui/acercade/AcercaDe.kt` (al revertir, **borrar** el archivo y la carpeta
    `ui/acercade/`): `AcercaDeScreen(onVolver: () -> Unit)` con `Surface` de fondo
    `colorScheme.background`, una `Column` con un `Encabezado` privado (`Row` con
    `.statusBarsPadding()` + `.padding(...)` y `BotonVolver(onClick = onVolver)`) y dos `Text`
    (título y versión).
  - `ui/navigation/LigorNavGraph.kt`: `const val ACERCA_DE = "acerca_de"` en `Rutas`;
    `import ...ui.acercade.AcercaDeScreen`; destino nuevo
    `composable(Rutas.ACERCA_DE) { AcercaDeScreen(onVolver = { navController.popBackStack() }) }`;
    y en el Perfil, `onAcercade = { navController.navigate(Rutas.ACERCA_DE) }`.
  - `ui/perfil/PerfilScreen.kt`: parámetro `onAcercade: () -> Unit` y un `Button` con ícono
    `Icons.Filled.Info` y texto "Acerca de LigorTravel" (más los imports de `Info` y `blur`).
- **Pendientes de diseño que el estudiante decidió no hacer** (no eran el objetivo del reto):
  padding y jerarquía de los textos, `Spacer` entre los botones del Perfil, `OutlinedButton` para la
  acción secundaria, `"$titulo"` → `titulo`, import `blur` sin usar y el nombre del archivo
  (`AcercaDeScreen.kt`).
- **Qué practicó:** crear una pantalla desde cero y conectarla a la navegación de la app, de ida y de
  vuelta.
- **Conceptos utilizados:**
  - **Las tres piezas de la navegación en Compose:** (1) una **ruta** (`Rutas.ACERCA_DE`), (2) un
    **destino registrado** en el `NavHost` con `composable(ruta) { ... }` que dice qué pantalla
    dibujar, y (3) una llamada a **`navigate(ruta)`**. Si falta el destino, `navigate` lanza
    `IllegalArgumentException: Navigation destination ... cannot be found` y la app se cierra
    (compila igual: el error es en ejecución).
  - **Back stack y `popBackStack()`:** `navigate` apila la pantalla nueva sobre el Perfil;
    `popBackStack()` la saca y vuelve a la anterior. El gesto de atrás del sistema hace lo mismo
    automáticamente.
  - **Pantallas que no navegan por su cuenta:** `AcercaDeScreen` recibe `onVolver` y `PerfilScreen`
    recibe `onAcercade`; solo `LigorNavGraph` (dueño del `navController`) decide a dónde ir. Así
    las pantallas son reutilizables y fáciles de previsualizar.
  - **Composable público vs. `private`:** `AcercaDeScreen` es pública porque la usa el NavGraph
    desde otro paquete (con su `import`); `Encabezado` es `private` porque solo la usa esa pantalla.
  - **Reutilizar componentes compartidos:** `BotonVolver` de `ui/components/DetalleComponentes.kt`
    en vez de crear otro botón.
  - **Declarar no es mostrar:** las variables `titulo` y `texto` no aparecían hasta pasarlas a un
    `Text(...)`; la UI es solo lo que los composables emiten.
  - **Edge-to-edge e insets:** la app dibuja detrás de la barra de estado; `.statusBarsPadding()`
    deja el espacio justo para que el contenido no quede tapado por la hora y la batería.
  - **Colores del tema:** `MaterialTheme.colorScheme.background` / `onBackground` /
    `onSurfaceVariant` en vez de `Color(0x...)`, siguiendo la paleta única de la app.

- **Repaso (2026-10-19):** ✅ Cumplido **tras 3 revisiones** (igual que la primera vez); el estudiante dijo
  "ya quedó". Archivo nuevo `ui/acercade/AcercaDeScreen.kt` (esta vez con el sufijo `Screen`; al revertir,
  borrarlo junto con la carpeta). NavGraph: `Rutas.ACERCA_DE`, `composable(Rutas.ACERCA_DE)` con
  `popBackStack()` y `onAcercaDe = {navController.navigate(Rutas.ACERCA_DE)}`. Perfil: parámetro
  `onAcercaDe` y `Button` "Acerca de LigorTravel" con `Spacer`. Pantalla: `Encabezado(onVolver)` con
  `.statusBarsPadding()` y `BotonVolver(onVolver)`, más dos `Text` (título y "Versión 1.0").

  Comparado con la primera vez: las tres piezas de la navegación (ruta, destino registrado y
  `navigate`) quedaron bien desde el inicio (antes faltó registrar el destino y la app se cerraba), y
  puso `statusBarsPadding` sin pista. Errores del repaso: `Encabezado { onVolver }` (una lambda que
  solo menciona el callback, el mismo concepto del repaso del Reto 5), el `Row` del encabezado vacío y
  las variables de texto declaradas sin mostrarse (se repitió "declarar no es mostrar"). Pendiente
  menor: `var texto` en vez de `val` y una `Column` interna innecesaria.

---

## Reto 7 — Confirmar antes de cerrar sesión

- **Nivel:** Nivel 1 — Puntual
- **Tipo:** Retroalimentación al usuario
- **Fecha:** 2026-10-17
- **Estado:** ✅ Cumplido (2026-10-17, tras 5 correcciones; probado en la app por el estudiante)
- **Pedido:** En el Perfil, al tocar "Cerrar sesión" no se debe salir de inmediato: primero aparece
  una ventana que pregunta "¿Seguro que deseas cerrar sesión?" con dos opciones. "Cerrar sesión"
  sale de la cuenta como hoy; "Cancelar" cierra la ventana y deja al usuario en su Perfil, sin
  cambios.
- **Punto de partida:** el del Reto 6 + las soluciones de los Retos 1 a 6 ya aplicadas.
- **Código original a restaurar:** copia completa de los archivos que podría tocar en
  `.claude/retoscumplidos/originales/reto7/` (rutas relativas a
  `app/src/main/java/com/example/ligortravel/`): `ui/perfil/PerfilScreen.kt`,
  `ui/perfil/PerfilViewModel.kt` y `ui/navigation/LigorNavGraph.kt`, más
  `lista_archivos_kt.txt` para detectar archivos nuevos.
- **Solución del estudiante** (rutas relativas a `app/src/main/java/com/example/ligortravel/`;
  `LigorNavGraph.kt` quedó igual que el original y no se crearon archivos):
  - `ui/perfil/PerfilViewModel.kt`: `val mostrarDialogoSesion: Boolean = false` en `PerfilUiState`,
    más `abrirDialogoCerrarSesion()` / `cerrarDialogoCerrarSesion()` (cada una con `copy(...)`).
  - `ui/perfil/PerfilScreen.kt`:
    - el botón "Cerrar sesión" del Perfil ahora solo llama a `viewModel.abrirDialogoCerrarSesion()`;
    - debajo, el diálogo se dibuja según el estado:

      ```kotlin
      if (uiState.mostrarDialogoSesion){
          DialogoCerrarSesion(viewModel = viewModel,
              onConfirmar = {
                  viewModel.cerrarSesion()
                  onCerrarSesion()
              }
          )
      }
      ```
    - composable privado nuevo `DialogoCerrarSesion(viewModel, onConfirmar)` con un `AlertDialog`:
      `onDismissRequest` y "Cancelar" → `viewModel::cerrarDialogoCerrarSesion`; "Cerrar Sesión" →
      `onClick = onConfirmar`.
- **Qué practicó:** mostrar un diálogo de confirmación controlado por estado, y separar *lo que se
  dibuja* de *lo que se ejecuta al tocar*.
- **Conceptos utilizados:**
  - **Diálogo vs. pantalla:** un `AlertDialog` no es una ruta de navegación; aparece encima de la
    pantalla actual y se muestra u oculta con un `Boolean` del estado
    (`if (uiState.mostrarDialogoSesion) { ... }`). El primer intento (una ruta
    `CONFIRMAR_CERRAR_SESION` en el NavGraph) era el enfoque equivocado.
  - **Partes de un `AlertDialog`:** `title`/`text` (lo que se lee), `confirmButton`,
    `dismissButton` y `onDismissRequest` (tocar fuera o el gesto de atrás). `onDismissRequest`
    debe comportarse como Cancelar, nunca ejecutar la acción destructiva.
  - **Composición vs. eventos (el concepto central):** el código dentro de un composable (el cuerpo
    de un `if`, el contenido `{ }` de un `TextButton`) se ejecuta **cada vez que se dibuja**. Solo lo
    que está dentro de `onClick = { ... }` espera al toque. Llamar a `cerrarSesion()` en el `if`
    cerraba la sesión apenas aparecía el diálogo.
  - **Contenido de un botón vs. su acción:** en `TextButton(onClick = ...) { Text(...) }`, las
    llaves finales son **lo que se ve** dentro del botón (*trailing lambda*), no lo que hace.
  - **Lambdas como "acciones para después":** `onConfirmar = { viewModel.cerrarSesion(); onCerrarSesion() }`
    guarda el código sin ejecutarlo; el diálogo lo dispara solo al tocar el botón.
  - **¿Dónde se arma cada callback?:** el NavGraph (dueño del `navController`) da `onCerrarSesion`;
    `PerfilScreen` (que tiene ese callback y el ViewModel) arma `onConfirmar`; el diálogo solo lo
    ejecuta. Un parámetro sube de nivel solo si ese nivel no puede armarlo. Agregar `onConfirmar`
    a `PerfilScreen` obligaba al NavGraph a pasarlo ("No value passed for parameter").
  - **Cerrar sesión = dos acciones:** `viewModel.cerrarSesion()` (Firebase + `SessionManager`) y
    `onCerrarSesion()` (navegar al login); separadas, se queda sin sesión en el Perfil o se llega al
    login con la sesión abierta.
  - **Patrón reutilizado:** el mismo esquema que el diálogo de "Actualizar contraseña"
    (`mostrarDialogo` + `abrirDialogoPassword()` / `cerrarDialogoPassword()`), con un nombre de
    estado propio para no mezclar los dos diálogos.
- **Repaso (2026-10-19):** ✅ Cumplido **tras 3 revisiones** (la primera vez fueron 5 correcciones) y probado en
  el emulador (las 5 pruebas: abrir, Cancelar, tocar fuera, solo el diálogo de contraseña, confirmar → login).
  Solución final igual a la original: `mostrarDialogoSesion` propio en `PerfilUiState`,
  `abrirDialogoCerrarSesion()` / `cerrarDialgoCerrarSesion()` (con error de tipeo), botón del Perfil que solo
  abre el diálogo, `if (uiState.mostrarDialogoSesion)` que arma `onConfirmar = { viewModel.cerrarSesion();
  onCerrarSesion() }` y `DialogoCerrarSesion(viewModel, onConfirmar)` con `onClick = {onConfirmar()}`.
  `LigorNavGraph.kt` sin cambios.

  Comparado con la primera vez: no intentó crear una ruta de navegación y no ejecutó acciones durante el
  dibujo (dentro del `if` o del contenido del botón): el concepto central del reto ya está aprendido. Errores
  del repaso: reutilizar el `Boolean` `mostrarDialogo` del diálogo de contraseña (se abrían los dos diálogos),
  `onDismissRequest` que volvía a abrir, "Cancelar" con la función de la contraseña, y el renombre
  `onConfirmar` aplicado al parámetro de `PerfilScreen` en vez del diálogo. Concepto reforzado: **cada
  diálogo con su propio estado** y distinguir el callback de navegación (`onCerrarSesion`, viene del NavGraph)
  de la acción completa (`onConfirmar`, se arma en la pantalla).

---

## 🏁 Resumen del repaso (2026-10-18 → 2026-10-19)

| # | Reto | Primera vez | Repaso |
|---|------|-------------|--------|
| 1 | Validación del nombre | 1 corrección | ✅ Al primer intento |
| 2 | Precios con 2 decimales | 3 correcciones | ✅ Al primer intento |
| 3 | Campo "Ciudad" (Nivel 2) | 3 correcciones | 1 revisión |
| 4 | Botón "Reintentar" | 3 correcciones | 3 revisiones (errores distintos) |
| 5 | Reserva → paquete (Nivel 2) | 5 correcciones | 2 revisiones |
| 6 | Pantalla "Acerca de" | 3 revisiones | 3 revisiones (navegación bien desde el inicio) |
| 7 | Confirmar cerrar sesión | 5 correcciones | 3 revisiones |

**Errores que se repitieron en ambas vueltas (para reforzar):**
- Olvidar incluir un campo nuevo al construir el modelo que se guarda con `set()` (Reto 3).
- No asignar el `launch` al `Job` (el `?.cancel()` falla en silencio) (Reto 4).
- Usar `reserva.id` en vez de `reserva.paqueteId` (Reto 5).
- "Declarar no es mostrar": variables de texto sin `Text` (Reto 6).

**Patrón nuevo del repaso:** lambdas que solo *mencionan* algo en vez de llamarlo o pasarlo
(`{ item.reserva.id }`, `Encabezado { onVolver }`). Regla: si ya tienes la función, pásala tal cual; una
lambda `{ ... }` solo hace falta para *hacer* algo adentro.

---

## Reto 8 — Nota opcional en la reserva

- **Nivel:** Nivel 2 — Atraviesa la app
- **Tipo:** Campo nuevo de punta a punta (con reglas de seguridad)
- **Fecha:** 2026-10-19
- **Estado:** ✅ Cumplido (2026-10-19) **con guía paso a paso** (la pidió el estudiante), tras 3 revisiones; probado en el emulador
- **Pedido:** En el formulario de reserva, agregar un campo opcional "Nota para el viaje" (máx. 100
  caracteres; si se pasa, mensaje y no se guarda). Se guarda con la reserva y se ve en la tarjeta de Mis
  reservas solo si tiene texto. Las reservas antiguas sin nota no deben romper nada. Las reglas de Firestore
  de `reservas` limitan los campos (`keys().hasOnly([...])`), así que hay que ajustarlas en la consola.
- **Punto de partida:** commit `28db33c` + las soluciones del repaso de los Retos 1 a 7 (sin commitear).
- **Código original a restaurar:** copia completa en `.claude/retoscumplidos/originales/reto8/` (rutas
  relativas a `app/src/main/java/com/example/ligortravel/`): `data/Reserva.kt`, `data/ReservaRepository.kt`,
  `ui/reserva/ReservaScreen.kt`, `ui/reserva/ReservaViewModel.kt`, `ui/reserva/ReglasReserva.kt`,
  `ui/misreservas/MisReservasScreen.kt` y `ui/misreservas/HistorialReservas.kt`, más `lista_archivos_kt.txt`.
  **Reglas de Firestore originales** de `reservas`: la copia de referencia está en `CLAUDE.md`; al revertir,
  volver a publicarlas tal cual en la consola.
- **Solución** (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  - `data/Reserva.kt`: `val nota: String = ""`.
  - `ui/reserva/ReservaViewModel.kt`: `nota` en `ReservaUiState`, `onNotaChange()`, `validar(..., estado.nota)`
    en `confirmarReserva()` y `nota = estado.nota.trim()` al construir la `Reserva`.
  - `ui/reserva/ReglasReserva.kt`: `NOTA_MAX = 100` y parámetro `nota: String = ""` en `validar` con la rama
    "La nota puede tener como máximo...". El valor por defecto mantuvo compilando `ReglasReservaTest`.
  - `ui/reserva/ReservaScreen.kt`: `CampoNota(nota, habilitado, onNotaChange)` con `OutlinedTextField` y
    contador en `supportingText`; `Seccion("Nota para el viaje (opcional)")` en `FormularioReserva`;
    `onNotaChange = viewModel::onNotaChange`.
  - `ui/misreservas/MisReservasScreen.kt`: en `TarjetaReserva`, `if (reserva.nota.isNotBlank())` con un `Text`
    de 2 líneas máx. y `TextOverflow.Ellipsis`.
  - **Reglas de Firestore (consola):** `'nota'` agregado a `hasOnly([...])` y
    `&& (!('nota' in request.resource.data) || (request.resource.data.nota is string && request.resource.data.nota.size() <= 100))`.
    **Al revertir: volver a publicar las reglas de `reservas` de CLAUDE.md**, que no tienen `nota`.
- **Errores del camino:** estado de la nota con `remember` dentro del campo (el ViewModel no la veía); una rama
  duplicada `paquete != null -> CampoNota()` en el `when` (nunca se alcanza); `onvalueChange` en minúscula;
  `isBlank()` en vez de `isNotBlank()`; y en las reglas, `request.data` en vez de `request.resource.data`
  (habría rechazado TODAS las reservas).
- **Hallazgo de seguridad (fuera del reto):** las reglas publicadas tenían `allow write: if request.auth != null`
  (TEMPORAL de la siembra) en `experiencias` y `paquetes`, lo que permite a cualquier usuario cambiar el precio de
  un paquete y anular la validación de precio de las reservas. Se recomendó cerrarlas con `if false`.
- **Conceptos utilizados:**
  - **Reglas con lista blanca (`keys().hasOnly([...])`):** el servidor rechaza cualquier campo que no esté en la
    lista; un campo nuevo exige actualizar las reglas, aunque el código esté perfecto.
  - **Validación en tres capas:** UI (contador), ViewModel (`ReglasReserva.validar`) y servidor (reglas).
  - **Parámetros con valor por defecto** para extender una función sin romper a quienes ya la llaman (tests).
  - **Estado elevado (state hoisting):** el campo no guarda estado; recibe `valor` + `onCambio` y el valor vive
    en el `UiState` para que el ViewModel lo use al guardar.
  - **`when` se queda con la primera rama verdadera:** dos ramas con la misma condición → la segunda nunca corre.
  - **Mostrar condicionalmente** con `if (...isNotBlank())`, y compatibilidad con documentos antiguos gracias al
    valor por defecto del modelo.
  - **`request.resource.data`** (lo que se intenta escribir) vs. `resource.data` (lo ya guardado); `request.data`
    no existe, y un error al evaluar una regla equivale a denegar.

---

## Reto 9 — Traslado desde el hotel en la reserva

- **Nivel:** Nivel 2 — Atraviesa la app
- **Tipo:** Campo nuevo de punta a punta (con reglas de seguridad) — práctica del Reto 8 **sin guía**
- **Fecha:** 2026-10-19
- **Estado:** 🟡 En curso (aún no se implementa)
- **Pedido:** En el formulario de reserva, una opción de sí/no "Necesito traslado desde mi hotel" (por defecto
  no). Se guarda con la reserva; en Mis reservas, las que lo pidieron muestran una indicación "Con traslado".
  Reservas antiguas sin el dato se ven sin errores (como "sin traslado"). Hay que ajustar las reglas de
  `reservas` (lista de campos y tipo del dato).
- **Punto de partida:** el del Reto 8 + su solución ya aplicada (reglas con `nota` publicadas).
- **Código original a restaurar:** copia completa en `.claude/retoscumplidos/originales/reto9/` (rutas
  relativas a `app/src/main/java/com/example/ligortravel/`): `data/Reserva.kt`, `ui/reserva/ReservaScreen.kt`,
  `ui/reserva/ReservaViewModel.kt`, `ui/reserva/ReglasReserva.kt` y `ui/misreservas/MisReservasScreen.kt`, más
  `lista_archivos_kt.txt`. Reglas: las del Reto 8 (con `nota`).

---

## Reto 10 — Recuperar contraseña ("¿Olvidaste?")

- **Nivel:** Nivel 2 — Atraviesa la app (pantalla nueva → ViewModel → repositorio → Firebase Auth → navegación)
- **Tipo:** Navegación + acción nueva contra el backend (lo pidió el estudiante; antes el botón era solo visual
  por decisión del 2026-09-15)
- **Fecha:** 2026-10-20
- **Estado:** 🟡 En curso (el estudiante ya había empezado: `ui/olvidarsesion/OlvidarSesionScreen.kt` vacío y el
  `onClick` del "¿Olvidaste?" vaciado en `AuthScreen`)
- **Pedido:** "¿Olvidaste?" abre una pantalla "Recuperar contraseña" con campo de correo y botón "Enviar enlace".
  Correo con formato inválido → mensaje, no se envía. Mientras envía: indicador de carga y botón deshabilitado.
  Al terminar: mensaje genérico de que, si existe una cuenta con ese correo, llegará un enlace (revisar spam).
  Errores de Firebase (sin internet, demasiados intentos) en español. Botón para volver al login.
- **Punto de partida:** commit `28db33c` (el código estaba igual a HEAD) + lo que el estudiante ya había empezado.
- **Código original a restaurar:** `git restore` de `app/` a `28db33c` y **borrar la carpeta**
  `ui/olvidarsesion/` (y cualquier archivo nuevo). Firebase no requiere cambios de reglas (Auth no usa reglas de
  Firestore).

---

## 🧭 Serie de navegación (desde 2026-11-06)

El estudiante quiere practicar navegación "en todas las pantallas posibles", de a poco: primero llegar a una
pantalla vacía y después ir llenándola. Los requerimientos 1–5 (chat, panel, reservas, mapa, reseñas) están
**sin commitear** en la rama `chat-ligor`: al revertir un reto, restaurar solo desde `originales/retoN/`
(nunca `git restore`, que borraría esos requerimientos).

## Reto 11 — Pantalla vacía desde "Favoritos"

- **Nivel:** Nivel 1 — Puntual
- **Tipo:** Navegación (pantalla nueva vacía)
- **Fecha:** 2026-11-06
- **Estado:** ✅ Cumplido (2026-11-06, tras 1 revisión)
- **Pedido:** En el Home, al tocar "Favoritos" en la barra de abajo (hoy no hace nada), debe abrirse una
  pantalla nueva, por ahora vacía. Con el gesto o botón "atrás" del celular se vuelve al Home.
- **Punto de partida:** rama `chat-ligor` con los requerimientos 1–5 sin commitear. Foto de git en
  `originales/reto11/git_status_inicio.txt` y `git_diff_stat_inicio.txt`.
- **Código original a restaurar:** copia de los archivos que podría tocar en
  `.claude/retoscumplidos/originales/reto11/` (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  `ui/home/HomeScreen.kt` y `ui/navigation/LigorNavGraph.kt`. `lista_archivos_kt.txt` tiene los `.kt` que
  existían antes: al revertir, **borrar** los archivos nuevos (la pantalla nueva).
- **Solución del estudiante** (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  - **Archivo nuevo** `ui/favoritos/FavoritosScreen.kt` (al revertir, **borrar** el archivo y la carpeta
    `ui/favoritos/`; además está en el stage de git con `git add`, así que hay que sacarlo con
    `git rm --cached` o `git restore --staged`): `@Composable fun FavoritosScreen(onVolver: () -> Unit)` vacía.
  - `ui/navigation/LigorNavGraph.kt`: `const val FAVORITOS = "favoritos"` en `Rutas`; import de
    `FavoritosScreen`; en el Home, `onIrAFavoritos = { navController.navigate(Rutas.FAVORITOS) }`; destino nuevo
    `composable(route = Rutas.FAVORITOS) { FavoritosScreen(onVolver = { navController.popBackStack() }) }`.
  - `ui/home/HomeScreen.kt`: parámetro `onIrAFavoritos` en `HomeScreen` y en `BottomNavHome`, y
    `onClick = onIrAFavoritos` en el `ItemNavVisual` de Favoritos.
- **Revisión:** faltaba `@Composable` en `FavoritosScreen`. Compilaba porque la función estaba vacía, pero habría
  fallado al poner el primer `Text` en el reto siguiente. Lo corrigió con la pista "compárala con las otras
  pantallas".
- **Pendientes menores que no eran el objetivo:** formato del bloque `composable (...)` en el NavGraph
  (`Ctrl + Alt + L`) y `onVolver` todavía sin usar (el gesto atrás del sistema ya vuelve al Home).
- **Qué practicó:** conectar una pantalla nueva a la navegación desde un botón que ya existía (la barra
  inferior del Home).
- **Conceptos utilizados:**
  - **Las tres piezas de la navegación:** ruta (`Rutas.FAVORITOS`), destino registrado en el `NavHost`
    (`composable(ruta) { ... }`) y `navigate(ruta)`. Las tres quedaron bien al primer intento.
  - **Pasar el evento hacia arriba (state hoisting de eventos):** el ítem de la barra recibe `onClick`,
    `BottomNavHome` y `HomeScreen` reciben `onIrAFavoritos`, y solo `LigorNavGraph`, dueño del `navController`,
    decide a dónde ir. El mismo patrón que `onIrAPaquetes` y `onIrAlMapa`.
  - **`@Composable`:** la anotación que convierte una función en parte de la UI de Compose. Solo una función
    `@Composable` puede llamar a otras como `Text` o `Column`; una función normal que no llama a ninguna compila
    igual, por eso el error no apareció hasta que se señaló.
  - **Back stack:** `navigate` apila Favoritos sobre el Home y el gesto atrás del sistema la saca, aunque la
    pantalla no tenga botón propio.

## Reto 12 — Título y mensaje en Favoritos

- **Nivel:** Nivel 1 — Puntual
- **Tipo:** Navegación (llenar la pantalla nueva)
- **Fecha:** 2026-11-06
- **Estado:** ✅ Cumplido (2026-11-06, tras 1 revisión; verificado en el celular)
- **Pedido:** En la pantalla de Favoritos, mostrar el título "Mis favoritos" y debajo el mensaje "Aún no tienes
  destinos favoritos". El texto no debe quedar tapado por la barra de estado (hora y batería).
- **Punto de partida:** el del Reto 11 con su solución aplicada. Foto de git en
  `originales/reto12/git_status_inicio.txt` y `git_diff_stat_inicio.txt`.
- **Código original a restaurar:** `.claude/retoscumplidos/originales/reto12/ui/favoritos/FavoritosScreen.kt`
  (la versión vacía del Reto 11). `lista_archivos_kt.txt`: borrar al revertir cualquier `.kt` nuevo.
- **Solución del estudiante** (`ui/favoritos/FavoritosScreen.kt`): `val Titulo` y `val Mensaje` con los textos,
  una `Column(modifier = Modifier.fillMaxSize().statusBarsPadding())` con `Text(Titulo)` y una `Column` interna
  con `Text(Mensaje)`.
- **Revisión:** en el primer intento solo había declarado las variables, sin mostrarlas (el "declarar no es
  mostrar" del Reto 6). Con la pista de usar el composable de texto, un contenedor vertical y el modificador de
  la barra de estado, lo resolvió.
- **Pendientes de diseño que no eran el objetivo:** fondo blanco (falta un `Surface` con
  `colorScheme.background`, como en las demás pantallas), "Aun" sin tilde, `Column` interna innecesaria, import
  `scale` sin usar y variables con mayúscula inicial (`titulo`, `mensaje`).
- **Qué practicó:** darle contenido a una pantalla nueva.
- **Conceptos utilizados:**
  - **Declarar no es mostrar:** una variable con texto no aparece hasta que se pasa a un `Text(...)`; la UI es
    solo lo que emiten los composables.
  - **`Column`:** acomoda sus hijos uno debajo del otro; sin contenedor, Compose los dibuja encimados.
  - **Modifier encadenado:** `.fillMaxSize()` ocupa toda la pantalla y `.statusBarsPadding()` deja el espacio de
    la barra de estado (edge-to-edge).
  - **Colores del tema:** sin un `Surface` que ponga el fondo, se ve el de la ventana de Android y el texto usa
    el color por defecto, por eso la pantalla salió clara en una app oscura.

## Reto 13 — Botón para volver en Favoritos

- **Nivel:** Nivel 1 — Puntual
- **Tipo:** Navegación (volver con `onVolver`)
- **Fecha:** 2026-11-06
- **Estado:** ✅ Cumplido (2026-11-06, tras 2 revisiones; en la segunda pidió ayuda y se le dio un fragmento)
- **Pedido:** En la pantalla de Favoritos, agregar arriba un botón para volver al Home, con el mismo estilo de las
  otras pantallas de la app (ej. Mis reservas o el Chat).
- **Punto de partida:** la solución del Reto 12. El estudiante empezó antes de que se guardara la copia, así que
  `originales/reto13/ui/favoritos/FavoritosScreen.kt` se reconstruyó con el contenido exacto revisado al cerrar el
  Reto 12. `lista_archivos_kt.txt`: borrar al revertir cualquier `.kt` nuevo.
- **Solución del estudiante** (`ui/favoritos/FavoritosScreen.kt`): `Surface(color = colorScheme.background)` con
  una `Column(fillMaxSize, statusBarsPadding, navigationBarsPadding)` que contiene `Encabezado(onVolver)` y los dos
  `Text`. `Encabezado` es privado: un `Row(fillMaxWidth, padding(12.dp))` con `BotonVolver(onVolver)` (componente
  compartido de `ui/components/DetalleComponentes.kt`). El NavGraph no cambió: ya pasaba `popBackStack()`.
- **Revisiones:**
  1. El `Surface` con su `Column(fillMaxSize)` envolvía solo el encabezado y quedaba encima de la `Column` de los
     textos: el primer bloque ocupaba toda la altura y los textos quedaban fuera de la pantalla (en el celular
     solo se veía "Volver"). Además, `statusBarsPadding` estaba dos veces y sobraba `imePadding`.
  2. Movió los textos fuera de la `Column` principal, como dos bloques sueltos al mismo nivel: se dibujaban
     encimados arriba a la izquierda y, al estar fuera del `Surface`, en negro sobre fondo oscuro. Pidió ayuda
     ("¿cuál es el problema con mi texto?") y se le dio el fragmento con los textos dentro de la misma `Column`
     del encabezado.
- **Pendientes menores:** "Aun" sin tilde, textos sin `padding` horizontal, variables con mayúscula inicial e
  imports sin usar (`scale`, `imePadding`).
- **Qué practicó:** usar el callback `onVolver` que la pantalla recibe y reutilizar el encabezado estándar de la
  app.
- **Conceptos utilizados:**
  - **Volver con un callback:** la pantalla no conoce el `navController`; llama a `onVolver` y el NavGraph decide
    (`popBackStack()`).
  - **Reutilizar componentes:** `BotonVolver` compartido en vez de crear otro botón; `Encabezado` privado porque
    solo lo usa esta pantalla.
  - **Jerarquía de layouts:** `fillMaxSize` dentro de un hijo de `Column` ocupa toda la altura que queda y empuja
    a los hermanos fuera de la vista; dos elementos sueltos sin `Column`/`Row` que los ordene se dibujan
    encimados.
  - **`Surface` da fondo y color de contenido:** lo que está adentro hereda el color de texto claro del tema
    (`LocalContentColor`); lo que queda afuera usa negro por defecto.

## Reto 14 — De Favoritos al mapa

- **Nivel:** Nivel 2 — Atraviesa la app (pantalla → NavGraph → otra pantalla existente)
- **Tipo:** Navegación desde la pantalla nueva hacia una existente
- **Fecha:** 2026-11-06
- **Estado:** ✅ Cumplido (2026-11-06, tras 1 revisión; verificado en el celular: Home → Favoritos → mapa → atrás vuelve a Favoritos)
- **Pedido:** En Favoritos, debajo del mensaje, un botón "Explorar destinos" que abra el mapa. Al volver atrás
  desde el mapa se regresa a Favoritos (no al Home).
- **Punto de partida:** la solución del Reto 13. Foto de git en `originales/reto14/`.
- **Código original a restaurar:** `originales/reto14/` (rutas relativas a
  `app/src/main/java/com/example/ligortravel/`): `ui/favoritos/FavoritosScreen.kt` y
  `ui/navigation/LigorNavGraph.kt`. `lista_archivos_kt.txt`: borrar al revertir cualquier `.kt` nuevo.
- **Solución del estudiante:**
  - `ui/favoritos/FavoritosScreen.kt`: parámetro nuevo `onIrAExplorar: () -> Unit` y, debajo del mensaje, un
    `Button(onClick = onIrAExplorar) { Text("Explorar destinos") }` (más el import de `Button`).
  - `ui/navigation/LigorNavGraph.kt`: en el destino de Favoritos,
    `onIrAExplorar = { navController.navigate(Rutas.MAPA) }`, reutilizando la ruta del mapa.
- **Revisión:** el primer intento tenía `onClick = {onIrAExplorar}`: una lambda que solo menciona la función sin
  llamarla (compila, pero el botón no hace nada). Es el mismo concepto que falló en el repaso del Reto 6
  (`Encabezado { onVolver }`). Lo corrigió pasando la función directamente.
- **Pendiente menor:** el botón quedó pegado al mensaje (un `Spacer` le daría aire).
- **Qué practicó:** navegar desde una pantalla nueva hacia otra que ya existía y controlar a dónde vuelve "atrás".
- **Conceptos utilizados:**
  - **Pasar una función vs. lambda que la menciona:** `onClick = onIrAExplorar` y `onClick = { onIrAExplorar() }`
    ejecutan la acción; `onClick = { onIrAExplorar }` solo nombra la función y no hace nada.
  - **El back stack decide a dónde se vuelve:** `navigate(Rutas.MAPA)` sin `popUpTo` apila el mapa encima de
    Favoritos (Home → Favoritos → Mapa); "atrás" saca el mapa y queda Favoritos.
  - **Reutilizar destinos de navegación:** el mapa ya estaba registrado en el `NavHost`; para llegar desde otra
    pantalla solo hace falta otro callback que llame a `navigate` con la misma ruta.
  - **Una pantalla, varios callbacks:** `FavoritosScreen(onVolver, onIrAExplorar)` declara todo lo que puede pedir
    y el NavGraph conecta cada uno.

## Reto 15 — Pantalla Notificaciones desde la campana

- **Nivel:** Nivel 1 — Puntual
- **Tipo:** Navegación (pantalla nueva con su texto). El estudiante pidió seguir creando pantallas nuevas, cada
  una con un texto referido a ella.
- **Fecha:** 2026-11-06
- **Estado:** ✅ Cumplido al primer intento (2026-11-06; verificado en el celular: campana → Notificaciones → atrás vuelve al Home)
- **Pedido:** En el Home, al tocar la campana de notificaciones (hoy solo visual), abrir una pantalla nueva
  "Notificaciones" con el texto "No tienes notificaciones nuevas". "Atrás" vuelve al Home.
- **Punto de partida:** la solución del Reto 14. Foto de git en `originales/reto15/`.
- **Código original a restaurar:** `originales/reto15/` (rutas relativas a
  `app/src/main/java/com/example/ligortravel/`): `ui/home/HomeScreen.kt` y `ui/navigation/LigorNavGraph.kt`.
  `lista_archivos_kt.txt`: borrar al revertir la pantalla nueva (y su carpeta).
- **Solución del estudiante** (rutas relativas a `app/src/main/java/com/example/ligortravel/`):
  - **Archivo nuevo** `ui/notificaciones/NotificacionesScreen.kt` (al revertir, borrar el archivo y la carpeta):
    `NotificacionesScreen(onVolver)` con `Surface(colorScheme.background)` y una `Column(fillMaxSize,
    statusBarsPadding, navigationBarsPadding)` con un `Encabezado` privado (`BotonVolver` + `Spacer` + título
    "Notificaciones" en 20 sp y negrita) y `Text(mensaje)`.
  - `ui/navigation/LigorNavGraph.kt`: `Rutas.NOTIFICACIONES`, import, destino
    `composable(route = Rutas.NOTIFICACIONES) { NotificacionesScreen(onVolver = { popBackStack() }) }` y en el
    Home `onIrANotificaciones = { navController.navigate(Rutas.NOTIFICACIONES) }`.
  - `ui/home/HomeScreen.kt`: parámetro `onIrANotificaciones` en `HomeScreen` y en `EncabezadoHome`, y
    `onClick = onIrANotificaciones` en el `IconoAccion` de la campana (el composable ya aceptaba `onClick`).
- **Pendientes menores:** el mensaje sin `padding` horizontal (pegado al borde), import de `Button` sin usar y una
  `Column` que envuelve solo el título.
- **Qué practicó:** crear una pantalla nueva completa (navegación + contenido) en un solo paso, desde un ícono que
  ya existía.
- **Conceptos utilizados:** las tres piezas de la navegación; pasar el evento por varios niveles
  (`IconoAccion` → `EncabezadoHome` → `HomeScreen` → NavGraph); reutilizar parámetros que un composable ya tiene
  (`onClick` de `IconoAccion`); `Surface` + una sola `Column` + insets; encabezado reutilizable con
  `BotonVolver`. Aplicó sin pistas lo que en los Retos 11–13 necesitó revisiones (`@Composable`, `Surface`, textos
  dentro de la misma `Column`, `statusBarsPadding` una sola vez).

## 💾 Guardado (2026-11-06)

- Commit `85216c2`: requerimientos 2–5 (panel, reservas, mapa, reseñas) y el respaldo del chat, **sin** los retos.
- Commit `5a2231f`: solo los Retos 11–15 (Favoritos, Notificaciones y sus conexiones en Home y NavGraph).
- **Para revertir los retos ahora basta `git revert 5a2231f`** (o restaurar desde `originales/reto11/`).
- Sin commitear a propósito: recuperar contraseña (Reto 10, del estudiante) y archivos `.idea`.
- **Próximo propuesto:** Reto 16 (Nivel 1) — "Ver todo" de Experiencias locales en el Home abre una pantalla
  nueva "Experiencias locales" con el texto "Aquí verás todas las experiencias locales".

## 🔄 Tercera reversión (2026-11-07)

Se revirtieron los **Retos 11–15** con el commit `6653688` ("Revertir los retos de navegación 11-15"), que deshace
`5a2231f` sin tocar el stage del estudiante: el contenido del proyecto quedó **idéntico a `85216c2`** (los cinco
requerimientos), compila y pasan los tests. Se borraron `ui/favoritos/` y `ui/notificaciones/`, y `HomeScreen.kt` y
`LigorNavGraph.kt` volvieron a su versión anterior.

- Las soluciones del estudiante quedaron en `.claude/retoscumplidos/soluciones_2026-11-06/` y también en el
  historial de git (`5a2231f`): `git cherry-pick 5a2231f` las recupera.
- **No se tocó el Reto 10** (recuperar contraseña): es trabajo del estudiante en curso y sin commitear; revertirlo
  lo habría borrado. Tampoco los archivos `.idea`.
- El Reto 16 propuesto ("Ver todo" de Experiencias locales) sigue disponible: no depende de los retos revertidos.

