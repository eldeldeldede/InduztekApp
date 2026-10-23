package cl.induztek.app.model

data class Resultado(
    val idResultado: Long,
    val codigo: String
)

// Los 3 resultados posibles de una prueba (RN-06 a RN-08b).
// IMPORTANTE: los ids están ORDENADOS de menor a mayor gravedad (1 < 2 < 3).
// Eso permite calcular el resultado global de un protocolo (RN-10: "el peor de sus
// pruebas") con un simple MAX(id_resultado) en SQL. Si cambias el orden, se rompe.
enum class TipoResultado(val id: Long, val codigo: String, val etiqueta: String) {
    APROBADO(1, "APROBADO", "Aprobado"),
    OBSERVADO(2, "OBSERVADO", "Observado"),
    RECHAZADO(3, "RECHAZADO", "Rechazado");

    companion object {
        fun desdeId(id: Long): TipoResultado = entries.first { it.id == id }
    }
}
