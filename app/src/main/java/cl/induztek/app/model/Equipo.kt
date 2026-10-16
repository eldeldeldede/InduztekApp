package cl.induztek.app.model

data class Equipo(
    val idEquipo: Long,
    val idInstalacion: Long,
    val tag: String,
    val idTipoEquipo: Long,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val potenciaKva: Double?,
    val tensiones: Double?,
    val corriente: Double?,
    val maxContactosUohm: Double
)
