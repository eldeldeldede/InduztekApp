package cl.induztek.app.model

import java.time.LocalDateTime

data class Visita(
    val idVisita: Long,
    val numeroOt: String,
    val idInstalacion: Long,
    val fecha: LocalDateTime,
    val tipoVisita: String,
    val idUsuario: Long, //foreign key con id del usuario tipo Ingeniero a cargo de la visita
    val fechaHoraCierre: LocalDateTime
)
