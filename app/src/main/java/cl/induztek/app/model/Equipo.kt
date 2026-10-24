package cl.induztek.app.model

data class Equipo(
    val idEquipo: Long = 0,
    val idInstalacion: Long,
    val tag: String,
    val idTipoEquipo: Long,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val anioFabricacion: Int? = null,
    val potenciaKva: Double? = null,
    val tensionPrimariaKv: Double? = null,
    val tensionSecundariaKv: Double? = null,
    val corrienteNominalA: Double? = null,
    val maxContactosUohm: Double? = null,
    val observaciones: String? = null,
    val fotoPlaca: String? = null,
    val tipoEquipoNombre: String = ""
)
