package cl.induztek.app.model

data class Medicion(
    val idMedicion: Long,
    val idPrueba: Long,
    val idParametro: Long,
    val valor: Double
)
