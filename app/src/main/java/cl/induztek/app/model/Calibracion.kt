package cl.induztek.app.model

import java.time.LocalDate

// CORREGIDO: LocalDateTime -> LocalDate. Una calibración se certifica por DÍA, no por hora.
data class Calibracion(
    val idCalibracion: Long,
    val idInstrumento: Long,
    val fechaCalibracion: LocalDate,
    val fechaVencimiento: LocalDate
)
