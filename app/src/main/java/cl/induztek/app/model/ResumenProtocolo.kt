package cl.induztek.app.model

import java.time.LocalDateTime

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

data class PuntoHistorial(
    val idProtocolo: Long,
    val numeroProtocolo: String,
    val fechaHoraPrueba: LocalDateTime,
    val valor: Double
)
