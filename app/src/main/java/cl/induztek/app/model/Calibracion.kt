package cl.induztek.app.model

import java.time.LocalDateTime

data class Calibracion(
    val idCalibracion: Long,
    val idInstrumento: Long,
    val fechaCalibracion: LocalDateTime,
    val fechaVencimiento: LocalDateTime
)
