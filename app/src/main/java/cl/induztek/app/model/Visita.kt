package cl.induztek.app.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class TipoVisita(val etiqueta: String) {
    SOLO_PRUEBAS("Solo pruebas"),
    PRUEBAS_Y_MANT("Pruebas y mantenimiento")
}

data class Visita(
    val idVisita: Long = 0,
    val numeroOt: String,
    val idInstalacion: Long,
    val fecha: LocalDate,
    val tipoVisita: TipoVisita,
    val idUsuario: Long, //foreign key con id del usuario tipo Ingeniero a cargo de la visita
    val fechaHoraCierre: LocalDateTime? = null
){
    val estaCerrada: Boolean get() = fechaHoraCierre != null
}
