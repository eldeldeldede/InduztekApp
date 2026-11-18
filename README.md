# InduztekApp

Aplicación Android para registrar en terreno los protocolos de prueba y mantención de equipos eléctricos (transformadores, relés de protección, celdas, cables) de **Induztek Ingeniería SpA**. Permite dejar un registro estructurado de cada prueba en el momento en que se realiza y consultar el historial de cada equipo, incluso sin conexión.

Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC), Evaluaciones Parciales 2 y 3. Todos los datos son ficticios.

Backend (microservicios Spring Boot): [DemoInduztek-microservicio](https://github.com/Pedro420-z/DemoInduztek-microservicio)

## Funcionalidades

- Inicio de sesión contra `usuarios-service` (con modo sin conexión para el último usuario que ingresó en el teléfono).
- Cambio obligatorio de la contraseña temporal en el primer ingreso.
- El Jefe Técnico crea los usuarios (elige el rol; la contraseña temporal la genera el servidor).
- Cuatro perfiles: Técnico, Ingeniero, Jefe Técnico y Cliente.
- Alta de equipos con TAG, tipo, datos de placa y foto de la placa.
- Creación de protocolos y registro de mediciones por prueba (aislación, TTR, devanados, contactos, operación de relé), con resultado calculado al instante.
- Validación de instrumentos (se bloquean los de calibración vencida) y de rangos de medida.
- Cierre de visita con acta y datos de quien recibe.
- Revisión del protocolo: formalizar, observar o anular, con motivo obligatorio.
- Historial de protocolos por equipo.
- Perfil Cliente: solo ve los equipos, visitas y actas de su propia empresa, y si cada protocolo está pendiente o formalizado.

## Recursos del dispositivo

- **Cámara**: fotografía de la placa del equipo (permiso `CAMERA`, `FileProvider`).
- **Estado de red**: indicador de conectividad (`ConnectivityManager`).

## Tecnologías

- Kotlin, Jetpack Compose y Material Design 3
- Arquitectura MVVM (`ViewModel`, `StateFlow`)
- Navigation Compose
- Persistencia local con SQLite (`SQLiteOpenHelper`) y `SharedPreferences` para la sesión
- Retrofit, OkHttp y Gson para consumir los microservicios

## Requisitos

- Android Studio actualizado (con soporte para `compileSdk`/`targetSdk` 37)
- JDK 11 o superior (el JDK integrado de Android Studio sirve)
- Dispositivo o emulador con Android 8.0 (API 26) o superior
- Conexión a internet la primera vez, para que Gradle descargue las dependencias
- Para el login: los microservicios corriendo en el PC (ver el repositorio del backend) y Laragon con MySQL encendido

## Cómo ejecutarla

1. Clonar el repositorio: `git clone https://github.com/eldeldeldede/InduztekApp.git`
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Encender MySQL en Laragon y levantar los microservicios (`iniciar-servicios.bat` en el repositorio del backend).
4. Elegir un emulador y presionar **Run**.

La app se conecta al PC desde el emulador por `10.0.2.2` (puertos 8081 a 8084, definidos en `network/ApiConfig.kt`). El proyecto usa `targetSdk = 36`: con Android 17 y `targetSdk` 37 el acceso a direcciones de red local requiere un permiso adicional.

La base SQLite local se crea sola, con datos de ejemplo, en el primer arranque.

## Usuarios

**Con los microservicios** (inicio de sesión real):

| Perfil | Correo | Contraseña |
| --- | --- | --- |
| Jefe Técnico inicial | jefe@induztek.cl | `Induztek2026` (temporal: la app pide cambiarla) |

Los demás usuarios los crea el Jefe desde el menú (**Crear usuario**). La contraseña temporal del nuevo usuario aparece en la consola de `usuarios-service` como `[CORREO SIMULADO]`.

**Datos de ejemplo locales** (las pantallas de equipos y protocolos aún leen SQLite local). Contraseña de todos: `Induztek2026`

| Perfil | Correo |
| --- | --- |
| Técnico | mgodoy@induztek.ejemplo |
| Ingeniero | acontreras@induztek.ejemplo |
| Jefe Técnico | respinoza@induztek.ejemplo |
| Cliente | mfuentes@envpacifico.ejemplo |

## Estado de la integración con los microservicios

- Conectado: inicio de sesión, cambio de contraseña y creación de usuarios.
- Pendiente: instalaciones, equipos, instrumentos, visitas y protocolos (siguen leyendo SQLite local), y el trabajo sin conexión con sincronización.

## Estructura

```
app/src/main/java/cl/induztek/app/
├── data/          Esquema SQLite, datos iniciales, sesión y seguridad
├── network/       Retrofit: cliente, direcciones, DTOs y resultados de red
├── model/         Entidades y estados de pantalla
├── validacion/    Reglas de negocio y validaciones en Kotlin puro
├── viewmodel/     ViewModels de cada pantalla
├── navigation/    Rutas y grafo de navegación
├── ui/            Pantallas, componentes reutilizables y tema
└── util/          Utilidades (fotos de placa)
```

## Pruebas

Pruebas unitarias en Kotlin puro (JUnit 4) para las reglas de negocio y validaciones, en `app/src/test`. Se ejecutan desde Android Studio: clic derecho en `app/src/test` → *Run Tests*.

Las pruebas eléctricas se modelan con una jerarquía sellada (`PruebaElectrica`) cuyas subclases implementan cada una su propia evaluación y validación de parámetros (herencia y polimorfismo). Las reglas de validación de formularios están centralizadas en `ValidacionesFormulario`.

## Equipo

- Gonzalo Bahamondes ([@eldeldeldede](https://github.com/eldeldeldede))
- Pedro Cid ([@Pedro420-z](https://github.com/Pedro420-z))

Tablero de trabajo: [Trello](https://trello.com/b/SjTLmmp4/proyecto-aplicaciones-moviles)
