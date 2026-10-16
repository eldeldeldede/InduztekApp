package cl.induztek.app.model

data class Parametro(
    val idParametro: Long,
    val idTipoPrueba: Long,
    val codigo: String,
    val nombre: String,
    val unidad: String
)
