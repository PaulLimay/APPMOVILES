# 1. Estado inicial / Splash

Kotlin es un lenguaje de **tipado estático** (cada dato tiene un tipo bien definido), pero tiene **inferencia de tipos** inteligente: el compilador adivina el tipo automáticamente si le das un valor inicial.

| Elemento | Detalle | 
| Qué ve el usuario | Logo de la app + indicador de carga breve | 
| Qué hace en segundo plano | Verifica si existe una sesión guardada (token/local storage) | 
| Resultado esperado | Si hay sesión activa → navega directo a Home autenticado. Si no hay sesión → navega a Autenticación | 
| Tiempo objetivo| < 2 segundos, sin bloquear la UI | 

# 2. Autenticación
## 2.1 Login
- Acción: el usuario ingresa correo y contraseña y presiona "Iniciar sesión".
- Resultado esperado: si las credenciales son válidas, navega a Home autenticado; si no, muestra un mensaje de error inline sin perder los datos ingresados.

## 2.2 Registro 
- Acción: el usuario completa nombre, correo, contraseña y confirmación.
- Resultado esperado: valida formato de correo y fortaleza de contraseña en tiempo real; al enviar, crea la cuenta y navega a Home autenticado (o a un paso de verificación de correo, si el equipo lo define así).

## 2.3 Recuperación de contraseña
- Acción: el usuario ingresa su correo y solicita recuperación.
- Resultado esperado: se envía un enlace/código de restablecimiento; el usuario define una nueva contraseña y vuelve a la pantalla de Login con un mensaje de confirmación.

# 3 Estado autenticado — Home y navegación
Al autenticarse, el usuario llega a un Home desde el que navega (bottom navigation o drawer, a definir por el equipo) hacia los módulos principales:
- Destinos — consulta de destinos y actividades disponibles en cada uno
- Planificador de viajes — itinerario con destinos y actividades, presupuesto estimado
- Reservas — gestión de paquetes turísticos y reservas realizadas
- Chat con Ligor — asistente virtual con recomendaciones basadas en preferencias
- Perfil — datos de la cuenta
Si el usuario autenticado tiene rol de administrador, el Home expone además un acceso a Panel administrativo, con:
- Gestión de usuarios
- Gestión de destinos
- Gestión de paquetes turísticos
- Reportes y estadísticas
# 4
_Siguiente: [Siga para ver el diagrama→](README.md)_
