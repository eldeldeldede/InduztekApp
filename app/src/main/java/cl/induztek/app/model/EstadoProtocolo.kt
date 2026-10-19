package cl.induztek.app.model

data class EstadoProtocolo(
    val idEstadoProtocolo: Long,
    val codigo: String
)

enum class TipoEstadoProtocolo(val codigo: String) {
    BORRADOR("BORRADOR"),
    REGISTRADO_TERRENO("REGISTRADO_TERRENO"),
    OBSERVADO("OBSERVADO"),
    FORMALIZADO("FORMALIZADO"),
    ANULADO("ANULADO")
}
