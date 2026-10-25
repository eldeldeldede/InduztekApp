# InduztekApp

Aplicación Android para registrar en terreno los protocolos de prueba y mantención de equipos eléctricos (transformadores, relés de protección, celdas, cables) de **Induztek Ingeniería SpA**. Permite dejar un registro estructurado de cada prueba en el momento en que se realiza y consultar el historial de cada equipo, incluso sin conexión.

Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC), Evaluación Parcial 2. Todos los datos son ficticios.

## Funcionalidades

- Inicio de sesión con cuatro perfiles: Técnico, Ingeniero, Jefe Técnico y Cliente.
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

## Requisitos

- Android Studio actualizado (con soporte para `compileSdk`/`targetSdk` 37)
- JDK 11 o superior (el JDK integrado de Android Studio sirve)
- Dispositivo o emulador con Android 8.0 (API 26) o superior
- Conexión a internet la primera vez, para que Gradle descargue las dependencias

## Cómo ejecutarla

1. Clonar el repositorio: `git clone https://github.com/eldeldeldede/InduztekApp.git`
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Elegir un emulador o dispositivo y presionar **Run**.

La base de datos se crea sola, con datos de ejemplo, en el primer arranque.

## Usuarios de prueba

Contraseña de todos: `Induztek2026`

| Perfil | Correo |
| --- | --- |
| Técnico | mgodoy@induztek.ejemplo |
| Ingeniero | acontreras@induztek.ejemplo |
| Jefe Técnico | respinoza@induztek.ejemplo |
| Cliente | mfuentes@envpacifico.ejemplo |

## Estructura

```
app/src/main/java/cl/induztek/app/
├── data/          Esquema SQLite, datos iniciales, sesión y seguridad
├── model/         Entidades y estados de pantalla
├── validacion/    Reglas de negocio y validaciones en Kotlin puro
├── viewmodel/     ViewModels de cada pantalla
├── navigation/    Rutas y grafo de navegación
├── ui/            Pantallas, componentes reutilizables y tema
└── util/          Utilidades (fotos de placa)
```

Las pruebas eléctricas se modelan con una jerarquía sellada (`PruebaElectrica`) cuyas subclases implementan cada una su propia evaluación y validación de parámetros (herencia y polimorfismo). Las reglas de validación de formularios están centralizadas en `ValidacionesFormulario`.

## Equipo

- Gonzalo Bahamondes ([@eldeldeldede](https://github.com/eldeldeldede))
- Pedro Cid ([@Pedro420-z](https://github.com/Pedro420-z))

Tablero de trabajo: [Trello](https://trello.com/b/SjTLmmp4/proyecto-aplicaciones-moviles)
