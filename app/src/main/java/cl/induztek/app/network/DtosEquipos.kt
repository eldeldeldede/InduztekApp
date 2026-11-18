package cl.induztek.app.network

import androidx.annotation.Keep

// =====================================================================
//  DtosEquipos.kt — la forma del JSON de equipos-service (puerto 8082)
//  Los nombres coinciden con los del microservicio (InstalacionResponse, EquipoResponse...).
//  Los números decimales llegan como Double y las fechas como texto ISO.
// =====================================================================

@Keep
data class InstalacionDto(
    val id: Long,
    val clienteId: Long,
    val nombre: String,
    val ubicacion: String?,
    val comunaId: Long,
    val comunaNombre: String?,
    val regionId: Long?,
    val regionNombre: String?,
    val observacionesAcceso: String?
)

@Keep
data class InstalacionSolicitud(
    val clienteId: Long?,
    val nombre: String,
    val ubicacion: String?,
    val comunaId: Long,
    val observacionesAcceso: String?
)

@Keep
data class EquipoDto(
    val id: Long,
    val instalacionId: Long,
    val instalacionNombre: String?,
    val clienteId: Long,
    val tag: String,
    val tipoEquipoId: Long,
    val tipoEquipoNombre: String?,
    val clasificacion: String?,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val anioFabricacion: Int?,
    val potenciaKva: Double?,
    val tensionPrimariaKv: Double?,
    val tensionSecundariaKv: Double?,
    val corrienteNominalA: Double?,
    val maxContactosUohm: Double?,
    val observaciones: String?
)

@Keep
data class EquipoSolicitud(
    val instalacionId: Long,
    val tag: String,
    val tipoEquipoId: Long,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val anioFabricacion: Int?,
    val potenciaKva: Double?,
    val tensionPrimariaKv: Double?,
    val tensionSecundariaKv: Double?,
    val corrienteNominalA: Double?,
    val maxContactosUohm: Double?,
    val observaciones: String?
)

@Keep
data class ComunaDto(val id: Long, val nombre: String, val regionId: Long, val regionNombre: String?)

@Keep
data class TipoEquipoDto(val id: Long, val nombre: String, val clasificacion: String)
