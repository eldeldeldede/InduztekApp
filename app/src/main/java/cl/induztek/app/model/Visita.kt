package cl.induztek.app.model

import java.time.LocalDate
import java.time.LocalDateTime

// Aclaración nº 4: una visita es "Solo pruebas" o "Pruebas y mantenimiento".
// `name` (SOLO_PRUEBAS / PRUEBAS_Y_MANT) es el texto que se guarda en la columna tipo_visita.
enum class TipoVisita(val etiqueta: String) {
    SOLO_PRUEBAS("Solo pruebas"),
    PRUEBAS_Y_MANT("Pruebas y mantenimiento")
}

data class Visita(
    val idVisita: Long = 0,
    val numeroOt: String,
    val idInstalacion: Long,
    val fecha: LocalDate,                  // CORREGIDO: la visita tiene fecha, no fecha+hora
    val tipoVisita: TipoVisita,            // CORREGIDO: enum en vez de String suelto
    val idUsuario: Long,                   // foreign key con id del usuario tipo Ingeniero a cargo de la visita
    val fechaHoraCierre: LocalDateTime? = null // CORREGIDO: null mientras la visita sigue abierta
) {
    val estaCerrada: Boolean get() = fechaHoraCierre != null
}
