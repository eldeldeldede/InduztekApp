package cl.induztek.app.model

enum class ClasificacionEquipo(val clasificacion: String) {
    PRIMARIO("Primario"),
    SECUNDARIO("Secundario")
}

data class TipoEquipo(
    val idTipoEquipo: Long,
    val nombre: String,
    val clasificacion: ClasificacionEquipo
)
