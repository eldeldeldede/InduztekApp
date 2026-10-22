package cl.induztek.app.model

import java.time.LocalDate

data class Calibracion(
    val idCalibracion: Long,
    val idInstrumento: Long,
    val fechaCalibracion: LocalDate,
    val fechaVencimiento: LocalDate
)
