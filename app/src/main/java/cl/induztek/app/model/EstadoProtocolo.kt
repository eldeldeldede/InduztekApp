package cl.induztek.app.model

data class EstadoProtocolo(
    val idEstadoProtocolo: Long,
    val codigo: String
)

enum class TipoEstadoProtocolo(val id: Long, val codigo: String, val etiqueta: String) {
    BORRADOR(1, "BORRADOR", "Borrador"),
    REGISTRADO_TERRENO(2, "REGISTRADO_TERRENO", "Registrado en terreno"),
    OBSERVADO(3, "OBSERVADO", "Observado"),
    FORMALIZADO(4, "FORMALIZADO", "Formalizado"),
    ANULADO(5, "ANULADO", "Anulado");

    fun etiquetaPara(esCliente: Boolean): String =
        if (esCliente && (this == REGISTRADO_TERRENO || this == OBSERVADO)) "Pendiente de formalización"
        else etiqueta

    companion object {
        fun desdeId(id: Long): TipoEstadoProtocolo = entries.first { it.id == id }
    }
}
