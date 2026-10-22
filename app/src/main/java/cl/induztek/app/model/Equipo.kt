package cl.induztek.app.model

data class Equipo(
    val idEquipo: Long = 0,
    val idInstalacion: Long,
    val tag: String,
    val idTipoEquipo: Long,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val potenciaKva: Double? = null,
    val tensionPrimariaKv: Double? = null,
    val tensionSecundariaKv: Double? = null,
    val corrienteNominal: Double? = null,
    val maxContactosUohm: Double? = null,
    val tipoEquipoNombre: String = ""
)
