package cl.induztek.app.model

enum class ClasificacionInstrumento(val clasificacion: String){
    PRIMARIO("Primario"),
    SECUNDARIO("Secundario")
}

data class TipoEquipo(
    val idTipoEquipo: Long,
    val nombre: String,
    val clasificacion: ClasificacionInstrumento
)
