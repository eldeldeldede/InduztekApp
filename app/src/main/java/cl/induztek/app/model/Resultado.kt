package cl.induztek.app.model

data class Resultado(
    val idResultado: Long,
    val codigo: String
)

enum class TipoResultado(val id: Long, val codigo: String, val etiqueta: String) {
    APROBADO(1, "APROBADO", "Aprobado"),
    OBSERVADO(2, "OBSERVADO", "Observado"),
    RECHAZADO(3, "RECHAZADO", "Rechazado");

    companion object {
        fun desdeId(id: Long): TipoResultado = entries.first { it.id == id }
    }
}
