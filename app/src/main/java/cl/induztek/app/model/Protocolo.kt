package cl.induztek.app.model

import java.time.LocalDateTime

data class Protocolo(
    val idProtocolo: Long = 0,
    val uuid: String,
    val numero: String,
    val idVisita: Long,
    val idEquipo: Long,
    val idUsuario: Long, // foreign key con id del usuario que registra los datos
    val fechaHoraPrueba: LocalDateTime,
    val temperaturaAmbiente: Double? = null, // Obligatorio junto a pctHumedad
    val pctHumedad: Double? = null,
    val descripcionMantenimiento: String? = null,
    val observaciones: String? = null,
    val idEstadoProtocolo: Long,
    val idProtocoloReemplazado: Long? = null
    )
