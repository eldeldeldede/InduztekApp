package cl.induztek.app.model

data class Resultado(
    val idResultado: Long,
    val codigo: String
)

enum class TipoResultado(val codigo: String){
    APROBADO("APROBADO"),
    OBSERVADO("OBSERVADO"),
    RECHAZADO("RECHAZADO")
}
