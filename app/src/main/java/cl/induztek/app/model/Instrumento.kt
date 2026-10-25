package cl.induztek.app.model

data class Instrumento(
    val idInstrumento: Long,
    val nombre: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String
)
