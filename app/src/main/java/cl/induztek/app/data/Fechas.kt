package cl.induztek.app.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// =====================================================================
//  Fechas.kt — convierte fechas de Kotlin <-> texto de SQLite
//  ---------------------------------------------------------------------
//  SQLite NO tiene un tipo "fecha": guarda TEXT. Nosotros guardamos las fechas
//  en formato ISO-8601, que tiene una gran ventaja:
//
//      "2026-09-26T13:42:00"   <- se ORDENA bien como texto (ORDER BY fecha DESC)
//
//  Así el historial "del más reciente al más antiguo" (HU-11) funciona sin trucos.
//
//  Uso:   fecha.aTexto()  ->  "2026-09-26"       texto.aFecha()  ->  LocalDate
//
//  NOTA: java.time necesita Android 8 (API 26). Por eso el build.gradle.kts
//  sube minSdk de 24 a 26.
// =====================================================================

// Siempre guardamos los segundos ("13:42:00"), incluso si son cero. Si usáramos
// toString() de LocalDateTime, "13:42:00" se guardaría como "13:42" y el orden
// de las fechas podría confundirse.
private val FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun LocalDate.aTexto(): String = toString() // "2026-09-26"
fun LocalDateTime.aTexto(): String = format(FORMATO_FECHA_HORA)

fun String.aFecha(): LocalDate = LocalDate.parse(this)
fun String.aFechaHora(): LocalDateTime = LocalDateTime.parse(this)
