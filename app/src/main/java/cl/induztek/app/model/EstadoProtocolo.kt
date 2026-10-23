package cl.induztek.app.model

// Una fila de la tabla "estado_protocolo".
data class EstadoProtocolo(
    val idEstadoProtocolo: Long,
    val codigo: String
)

// Los 5 estados de RN-04. Igual que CodigoRol: el `id` debe coincidir con DatosIniciales.kt.
// AGREGADO: `id` y `etiqueta`. Así el código escribe TipoEstadoProtocolo.BORRADOR.id
// en vez de un número mágico, y la pantalla muestra `etiqueta`.
enum class TipoEstadoProtocolo(val id: Long, val codigo: String, val etiqueta: String) {
    BORRADOR(1, "BORRADOR", "Borrador"),
    REGISTRADO_TERRENO(2, "REGISTRADO_TERRENO", "Registrado en terreno"),
    OBSERVADO(3, "OBSERVADO", "Observado"),
    FORMALIZADO(4, "FORMALIZADO", "Formalizado"),
    ANULADO(5, "ANULADO", "Anulado");

    // RN-19: el usuario CLIENTE ve "Pendiente de formalización" en vez de "Registrado en
    // terreno" u "Observado". Los demás estados se muestran con su etiqueta normal.
    fun etiquetaPara(esCliente: Boolean): String =
        if (esCliente && (this == REGISTRADO_TERRENO || this == OBSERVADO)) "Pendiente de formalización"
        else etiqueta

    companion object {
        fun desdeId(id: Long): TipoEstadoProtocolo = entries.first { it.id == id }
    }
}
