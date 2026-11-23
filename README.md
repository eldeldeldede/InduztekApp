# InduztekApp

Aplicación Android para registrar en terreno los protocolos de prueba y mantención de equipos eléctricos (transformadores, relés de protección, celdas, cables) de **Induztek Ingeniería SpA**. Permite dejar un registro estructurado de cada prueba en el momento en que se realiza y consultar el historial de cada equipo, incluso sin conexión.

Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC), Evaluaciones Parciales 2 y 3. Todos los datos son ficticios.

Backend (microservicios Spring Boot): [DemoInduztek-microservicio](https://github.com/Pedro420-z/DemoInduztek-microservicio)

## Funcionalidades

- Inicio de sesión contra `usuarios-service` (con modo sin conexión para el último usuario que ingresó en el teléfono).
- Cambio obligatorio de la contraseña temporal en el primer ingreso.
- El Jefe Técnico crea los usuarios (elige el rol; la contraseña temporal la genera el servidor y se envía por correo).
- Técnico e Ingeniero pueden crear clientes e instalaciones.
- Cuatro perfiles: Técnico, Ingeniero, Jefe Técnico y Cliente.
- Alta de equipos con TAG, tipo, datos de placa y foto de la placa. Solo el Jefe Técnico puede editar un equipo; los equipos no se eliminan.
- Creación de protocolos y registro de mediciones por prueba (aislación, TTR, devanados, contactos, operación de relé), con resultado calculado al instante.
- Validación de instrumentos (se bloquean los de calibración vencida) y de rangos de medida.
- Cierre de visita con acta y datos de quien recibe.
- Revisión del protocolo: formalizar, observar o anular, con motivo obligatorio.
- Historial de protocolos por equipo. Protocolos, visitas y actas no se eliminan; solo se puede eliminar una prueba de un protocolo, tras tres advertencias.
- Trabajo sin conexión: lo que se registra sin red queda en una cola local y se sincroniza solo al volver la conexión.
- Perfil Cliente: solo ve los equipos, visitas y actas de su propia empresa. Ve todos sus protocolos salvo borradores y anulados; los observados y los aún no formalizados aparecen como "Pendiente de formalización".

## Recursos del dispositivo

- **Cámara**: fotografía de la placa del equipo (permiso `CAMERA`, `FileProvider`).
- **Estado de red**: indicador de conectividad (`ConnectivityManager`).

## Tecnologías

- Kotlin, Jetpack Compose y Material Design 3
- Arquitectura MVVM (`ViewModel`, `StateFlow`)
- Navigation Compose
- Los datos viven en los microservicios (MySQL). En el teléfono solo se guardan la sesión (`SharedPreferences`), una caché de consultas y la cola de operaciones pendientes (archivos JSON)
- Retrofit, OkHttp y Gson para consumir los microservicios

## Requisitos

- Android Studio actualizado (con soporte para `compileSdk`/`targetSdk` 37)
- JDK 11 o superior (el JDK integrado de Android Studio sirve)
- Dispositivo o emulador con Android 8.0 (API 26) o superior
- Conexión a internet la primera vez, para que Gradle descargue las dependencias
- Los microservicios corriendo en el PC (ver el repositorio del backend) y Laragon con MySQL encendido

## Cómo ejecutarla

1. Clonar el repositorio: `git clone https://github.com/eldeldeldede/InduztekApp.git`
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Encender MySQL en Laragon y levantar Eureka, los microservicios y el API Gateway (`iniciar-servicios.bat`, o los pasos del README del backend).
4. Elegir un emulador y presionar **Run**.

La app se conecta al PC desde el emulador por `10.0.2.2` (API Gateway en el puerto 8080, definido en `network/ApiConfig.kt`; el gateway reparte las llamadas a los microservicios). El proyecto usa `targetSdk = 36`: con Android 17 y `targetSdk` 37 el acceso a direcciones de red local requiere un permiso adicional.

Al primer arranque del backend se cargan datos de ejemplo (empresas, usuarios, instrumentos, equipos y actas).

## Usuarios

Contraseña de todos: `Induztek2026` (temporal: la app pide cambiarla en el primer ingreso).

| Perfil | Correo |
| --- | --- |
| Jefe Técnico | jefe@induztek.cl |
| Ingeniero | camila.rojas@induztek.ejemplo |
| Técnico | diego.soto@induztek.ejemplo |
| Cliente | mfuentes@envpacifico.ejemplo |
| Cliente | tleiva@losaromos.ejemplo |

Los demás usuarios los crea el Jefe desde el menú (**Crear usuario**). La contraseña temporal llega al correo del nuevo usuario por Gmail (ver el README del backend).

## Estructura

```
app/src/main/java/cl/induztek/app/
├── data/          Sesión y seguridad
├── local/         Cola de operaciones pendientes y almacenamiento sin conexión
├── network/       Retrofit: cliente, direcciones, DTOs y resultados de red
├── repository/    Acceso a los microservicios, caché y sincronización
├── model/         Entidades y estados de pantalla
├── validacion/    Reglas de negocio y validaciones en Kotlin puro
├── viewmodel/     ViewModels de cada pantalla
├── navigation/    Rutas y grafo de navegación
├── ui/            Pantallas, componentes reutilizables y tema
└── util/          Utilidades (fotos de placa, conexión)
```

## Pruebas

10 pruebas unitarias en Kotlin puro (JUnit 4), repartidas en 4 archivos: criterios de resultado, validación de RUT, validaciones de recuperación y almacenamiento local, en `app/src/test`. Se ejecutan desde Android Studio: clic derecho en `app/src/test` → *Run Tests*.

Las pruebas eléctricas se modelan con una jerarquía sellada (`PruebaElectrica`) cuyas subclases implementan cada una su propia evaluación y validación de parámetros (herencia y polimorfismo). Las reglas de validación de formularios están centralizadas en `ValidacionesFormulario`.

## Equipo

- Gonzalo Bahamondes ([@eldeldeldede](https://github.com/eldeldeldede))
- Pedro Cid ([@Pedro420-z](https://github.com/Pedro420-z))

Tablero de trabajo: [Trello](https://trello.com/b/SjTLmmp4/proyecto-aplicaciones-moviles)
