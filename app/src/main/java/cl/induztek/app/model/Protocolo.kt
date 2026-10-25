package cl.induztek.app.model

import java.time.LocalDateTime

data class Protocolo(
    val idProtocolo: Long = 0,
    val uuid: String,
    val numero: String,
    val idVisita: Long,
    val idEquipo: Long,
    val idUsuario: Long,
    val fechaHoraPrueba: LocalDateTime,
    val temperaturaAmbiente: Double? = null,
    val pctHumedad: Double? = null,
    val descripcionMantenimiento: String? = null,
    val observaciones: String? = null,
    val idEstadoProtocolo: Long,
    val idProtocoloReemplazado: Long? = null
)
