package cl.induztek.app.model

import java.time.LocalDateTime

data class RecepcionActa(
    val idVisita: Long,
    val receptorNombre: String,
    val receptorRut: String,
    val receptorCargo: String,
    val fechaHoraRecepcion: LocalDateTime
)
