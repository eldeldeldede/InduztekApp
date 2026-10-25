package cl.induztek.app.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun LocalDate.aTexto(): String = toString()
fun LocalDateTime.aTexto(): String = format(FORMATO_FECHA_HORA)

fun String.aFecha(): LocalDate = LocalDate.parse(this)
fun String.aFechaHora(): LocalDateTime = LocalDateTime.parse(this)

private val FORMATO_FECHA_HORA_LEGIBLE = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
private val FORMATO_FECHA_LEGIBLE = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")

fun LocalDateTime.aTextoLegible(): String = format(FORMATO_FECHA_HORA_LEGIBLE)
fun LocalDate.aTextoLegible(): String = format(FORMATO_FECHA_LEGIBLE)
