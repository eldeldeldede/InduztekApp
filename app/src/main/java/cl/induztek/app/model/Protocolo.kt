package cl.induztek.app.model

import java.time.LocalDateTime

data class Protocolo(
    val idProtocolo: Long,
    val numero: String,
    val idVisita: Long,
    val idEquipo: Long,
    val idUsuario: Long, // foreign key con id del usuario que registra los datos
    val fechaHoraPrueba: LocalDateTime,
    val temperaturaAmbiente: Double, // Obligatorio junto a pctHumedad
    val pctHumedad: Double,
    val descripcionMantenimiento: String,
    val idEstadoProtocolo: Long,
    val idProtocoloReemplazado: Long?
    )
