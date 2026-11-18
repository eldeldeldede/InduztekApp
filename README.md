# InduztekApp

Aplicación Android para registrar en terreno los protocolos de prueba y mantención de equipos eléctricos (transformadores, relés de protección, celdas, cables) de **Induztek Ingeniería SpA**. Permite dejar un registro estructurado de cada prueba en el momento en que se realiza y consultar el historial de cada equipo, incluso sin conexión. Los clientes de Induztek acceden al historial de protocolos de los equipos de su propia empresa y pueden reportar problemas de un acta.

Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC), Evaluaciones Parciales 2 y 3. Todos los datos son ficticios.

Backend (microservicios Spring Boot): [DemoInduztek-microservicio](https://github.com/Pedro420-z/DemoInduztek-microservicio)

## Funcionalidades

**Cuenta y sesión**
- Inicio de sesión contra `usuarios-service`, con modo sin conexión para el último usuario que ingresó en el teléfono.
- Cambio obligatorio de la contraseña temporal en el primer ingreso.
- Recuperar contraseña: el servidor envía un código de 6 dígitos al correo y con él se define una contraseña nueva.
- Editar perfil (nombre y teléfono; los clientes también giro y contacto) y cambiar la contraseña cuando se quiera. El correo y el RUT no se modifican.
- Menú lateral que cambia según el rol, con cierre de sesión.

**Trabajo en terreno (Técnico, Ingeniero, Jefe Técnico)**
- Alta de equipos con TAG, tipo, datos de placa y foto de la placa.
- Creación de protocolos y registro de mediciones por prueba (aislación, TTR, devanados, contactos, operación de relé), con resultado calculado al instante.
- Validación de instrumentos (se bloquean los de calibración vencida) y de rangos de medida.
- Cierre de visita con acta y datos de quien recibe.
- Revisión del protocolo: formalizar, observar o anular, con motivo obligatorio.
- Historial de protocolos por equipo.

**Gestión (requiere conexión)**
- Clientes: el personal los busca, crea y edita; solo el Jefe Técnico activa o desactiva cuentas.
- Instalaciones (plantas o sitios de un cliente): el personal las crea y edita, eligiendo la comuna.
- Usuarios: solo el Jefe Técnico crea cuentas de cualquier rol (el servidor genera la contraseña temporal y la envía por correo). Técnico e Ingeniero solo pueden crear clientes.

**Reportes de actas (requiere conexión)**
- El cliente reporta un problema de un acta cerrada, opcionalmente indicando el protocolo observado. Si ese protocolo estaba formalizado, vuelve al estado *observado* para que el técnico lo corrija y el ingeniero lo formalice de nuevo.
- Cada reporte tiene seguimiento: *Abierto → En revisión → Resuelto* o *Rechazado*. Lo gestionan el Ingeniero y el Jefe Técnico con una respuesta escrita que ve el cliente; el Técnico solo consulta.
- Un acta puede tener varios reportes.

**Perfil Cliente**
- Solo ve los equipos, visitas, actas y protocolos de su propia empresa, y si cada protocolo está pendiente o formalizado.

## Modo sin conexión y sincronización

Con y sin señal la app permite crear protocolos, registrar mediciones, cerrar visitas y ver el último protocolo de cada equipo (se guarda en el teléfono lo que se consultó con conexión).

- Los cambios hechos sin conexión quedan en una cola local en orden y se muestran como **pendientes de sincronizar**.
- Al volver la red (`ConnectivityManager`) la app sincroniza sola y actualiza los datos.
- Si el servidor ya tiene un registro equivalente (por ejemplo, la misma visita o el mismo protocolo), se reutiliza en vez de duplicarlo. En conflictos de datos gana el último que llega. Una operación que el servidor rechaza queda marcada como fallida y no se pierde.
- Requieren conexión: crear usuarios y clientes, instalaciones, cambiar o recuperar la contraseña y los reportes de actas.

## Recursos del dispositivo

- **Cámara**: fotografía de la placa del equipo (permiso `CAMERA`, `FileProvider`).
- **Estado de red**: indicador de conectividad y disparador de la sincronización (`ConnectivityManager`).
- **Almacenamiento interno**: cola de operaciones y datos en caché, por usuario.

## Tecnologías

- Kotlin, Jetpack Compose y Material Design 3
- Arquitectura MVVM (`ViewModel`, `StateFlow`) con capa **Repository** entre los ViewModels y los microservicios
- Navigation Compose
- Retrofit, OkHttp y Gson para consumir los microservicios
- Almacenamiento local en archivos JSON y `SharedPreferences` para la sesión

## Requisitos

- Android Studio actualizado (con soporte para `compileSdk`/`targetSdk` 37)
- JDK 11 o superior (el JDK integrado de Android Studio sirve)
- Dispositivo o emulador con Android 8.0 (API 26) o superior
- Conexión a internet la primera vez, para que Gradle descargue las dependencias
- Los microservicios corriendo en el PC (ver el repositorio del backend), con Laragon y MySQL encendidos. Para los correos (contraseña temporal y recuperación) se usa SMTP en `localhost:1025`.

## Cómo ejecutarla

1. Clonar el repositorio: `git clone https://github.com/eldeldeldede/InduztekApp.git`
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Encender MySQL en Laragon y levantar los microservicios (`iniciar-servicios.bat` en el repositorio del backend).
4. Elegir un emulador y presionar **Run**.

La app se conecta al PC desde el emulador por `10.0.2.2` (puertos 8081 a 8084, definidos en `network/ApiConfig.kt`). El proyecto usa `targetSdk = 36`: con Android 17 y `targetSdk` 37 el acceso a direcciones de red local requiere un permiso adicional.

## Usuarios de ejemplo

Los microservicios dejan listos los datos de prueba: el Jefe Técnico inicial, dos empresas cliente con sus instalaciones, equipos y protocolos. Contraseña de todos: `Induztek2026` (en el primer ingreso la app pide cambiarla).

| Perfil | Correo |
| --- | --- |
| Jefe Técnico | jefe@induztek.cl |
| Ingeniero | camila.rojas@induztek.ejemplo |
| Técnico | diego.soto@induztek.ejemplo |
| Cliente (Envasadora del Pacífico) | mfuentes@envpacifico.ejemplo |
| Cliente (Los Aromos) | tleiva@losaromos.ejemplo |

El Jefe Técnico puede crear nuevos clientes, instalaciones y protocolos desde la app. Los correos que envía el sistema se pueden ver en la bandeja del servidor SMTP local.

## Estructura

```
app/src/main/java/cl/induztek/app/
├── data/          Sesión (SharedPreferences), seguridad y utilidades de fechas
├── network/       Retrofit: cliente, direcciones, DTOs, resultados de red y caché
├── repository/    Capa Repository: única puerta hacia los microservicios + sincronizador
├── local/         Cola de operaciones pendientes y almacenamiento local
├── model/         Entidades y estados de pantalla
├── validacion/    Reglas de negocio y validaciones en Kotlin puro
├── viewmodel/     ViewModels de cada pantalla
├── navigation/    Rutas y grafo de navegación
├── ui/            Pantallas, componentes reutilizables y tema
└── util/          Utilidades (fotos de placa, monitor de conexión)
```

## Pruebas

10 pruebas unitarias en Kotlin (JUnit 4), repartidas en 4 archivos de `app/src/test`:

| Archivo | Qué prueba |
| --- | --- |
| `ValidadorRutTest` | RUT válidos, dígito verificador incorrecto y formato con puntos y guion |
| `CriteriosResultadoTest` | Criterios de resultado de la prueba de aislación y resultado global del protocolo |
| `ValidacionesRecuperacionTest` | Validación del código de recuperación de contraseña (6 dígitos) |
| `AlmacenLocalTest` | Ids locales y cola de operaciones pendientes del modo sin conexión |

Se ejecutan desde Android Studio: clic derecho en `app/src/test` → *Run Tests*.

Las pruebas eléctricas se modelan con una jerarquía sellada (`PruebaElectrica`) cuyas subclases implementan cada una su propia evaluación y validación de parámetros (herencia y polimorfismo). Las reglas de validación de formularios están centralizadas en `ValidacionesFormulario`.

## Equipo

- Gonzalo Bahamondes ([@eldeldeldede](https://github.com/eldeldeldede))
- Pedro Cid ([@Pedro420-z](https://github.com/Pedro420-z))

Tablero de trabajo: [Trello](https://trello.com/b/SjTLmmp4/proyecto-aplicaciones-moviles)
