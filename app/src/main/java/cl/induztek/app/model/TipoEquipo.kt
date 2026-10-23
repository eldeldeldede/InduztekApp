package cl.induztek.app.model

// CORREGIDO: se llamaba ClasificacionInstrumento, pero clasifica EQUIPOS (Aclaración nº 1):
// Primario = transformador, interruptor, celda MT, cable. Secundario = relé de protección.
enum class ClasificacionEquipo(val clasificacion: String) {
    PRIMARIO("Primario"),
    SECUNDARIO("Secundario")
}

data class TipoEquipo(
    val idTipoEquipo: Long,
    val nombre: String,
    val clasificacion: ClasificacionEquipo
)
