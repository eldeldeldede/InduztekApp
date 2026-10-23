package cl.induztek.app.model

import java.time.LocalDateTime

// =====================================================================
//  ResumenProtocolo.kt — una "vista de lectura" para las LISTAS
//
//  No es una tabla: es el resultado de un SELECT con varios JOIN, pensado para
//  mostrar una fila de historial (HU-11, HU-12): número, fecha, resultado global y estado.
//  `resultadoGlobal` es null cuando el protocolo todavía no tiene pruebas con resultado.
// =====================================================================
data class ResumenProtocolo(
    val idProtocolo: Long,
    val numero: String,
    val idEquipo: Long,
    val tagEquipo: String,
    val fechaHoraPrueba: LocalDateTime,
    val estado: TipoEstadoProtocolo,
    val resultadoGlobal: TipoResultado?,
    val tecnicoNombre: String
)

// Un punto del historial de UN valor medido (por ejemplo la lectura a 1 min del AT-T del TR-01).
// Con la lista ordenada se calcula la variación contra la prueba anterior (RN-09, HU-06).
data class PuntoHistorial(
    val idProtocolo: Long,
    val numeroProtocolo: String,
    val fechaHoraPrueba: LocalDateTime,
    val valor: Double
)

// Un valor que se mide en un equipo: "Aislación · AT-T · Lectura 1 min (MΩ)".
// Con él se pide el historial de ese valor y se compara contra la prueba anterior (RN-09).
data class SerieMedicion(
    val codigoTipoPrueba: String,
    val nombreTipoPrueba: String,
    val elemento: String,
    val codigoParametro: String,
    val nombreParametro: String,
    val unidad: String
)
