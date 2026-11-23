package cl.induztek.app.network

import androidx.annotation.Keep

// =====================================================================
//  DtosProtocolos.kt — la forma del JSON de protocolos-service (8083) y reportes-service (8084)
//  reportes-service devuelve los protocolos con la misma forma (ProtocoloDto).
// =====================================================================

@Keep
data class MedicionDto(val parametroCodigo: String, val parametroNombre: String?, val unidad: String?, val valor: Double)

@Keep
data class PruebaDto(
    val id: Long,
    val tipoPruebaId: Long,
    val tipoPruebaCodigo: String,
    val tipoPruebaNombre: String?,
    val elemento: String,
    val momento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val instrumentoId: Long?,
    val resultado: String?,
    val valorCalculado: Double?,
    val comentario: String?,
    val mediciones: List<MedicionDto>?
)

@Keep
data class HistorialDto(
    val id: Long,
    val estadoAnterior: String?,
    val estadoNuevo: String,
    val usuarioId: Long,
    val fechaHora: String,
    val motivo: String?
)

@Keep
data class ProtocoloDto(
    val id: Long,
    val numero: String,
    val visitaId: Long,
    val visitaNumeroOt: String?,
    val equipoId: Long,
    val usuarioRegistraId: Long,
    val fechaHoraPrueba: String,
    val temperaturaC: Double?,
    val humedadPct: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?,
    val estado: String,
    val protocoloReemplazadoId: Long?,
    val resultadoGlobal: String?,
    val atrasado: Boolean,
    val pruebas: List<PruebaDto>?,
    val historial: List<HistorialDto>?
)

@Keep
data class RecepcionDto(
    val receptorNombre: String,
    val receptorRut: String,
    val receptorCargo: String,
    val fechaHoraRecepcion: String
)

@Keep
data class VisitaDto(
    val id: Long,
    val numeroOt: String,
    val instalacionId: Long,
    val fecha: String,
    val tipoVisita: String,
    val ingenieroId: Long,
    val participantesIds: List<Long>?,
    val fechaHoraCierre: String?,
    val cerrada: Boolean,
    val recepcion: RecepcionDto?
)

@Keep
data class ParametroDto(val id: Long, val codigo: String, val nombre: String, val unidad: String?)

@Keep
data class TipoPruebaDto(val id: Long, val codigo: String, val nombre: String, val parametros: List<ParametroDto>?)

@Keep
data class InstrumentoDto(
    val id: Long,
    val nombre: String,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String,
    val ultimaCalibracion: String?,
    val vencimientoCalibracion: String?,
    val vigente: Boolean
)

@Keep
data class VisitaSolicitud(
    val numeroOt: String,
    val instalacionId: Long,
    val fecha: String,
    val tipoVisita: String,
    val ingenieroId: Long,
    val participantesIds: List<Long>
)

@Keep
data class RecepcionSolicitud(val receptorNombre: String, val receptorRut: String, val receptorCargo: String)

@Keep
data class ProtocoloSolicitud(
    val visitaId: Long,
    val equipoId: Long,
    val fechaHoraPrueba: String?,
    val temperaturaC: Double?,
    val humedadPct: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?,
    val protocoloReemplazadoId: Long?
)

@Keep
data class ProtocoloActualizacionSolicitud(
    val fechaHoraPrueba: String?,
    val temperaturaC: Double?,
    val humedadPct: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?
)

@Keep
data class MedicionSolicitud(val parametro: String, val valor: Double)

@Keep
data class PruebaSolicitud(
    val tipoPruebaId: Long,
    val elemento: String,
    val momento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val instrumentoId: Long?,
    val noOpera: Boolean?,
    val resultadoManual: String?,
    val comentario: String?,
    val mediciones: List<MedicionSolicitud>?
)

@Keep
data class MotivoSolicitud(val motivo: String)

@Keep
data class ActaDto(val visita: VisitaDto, val instalacionNombre: String?, val protocolos: List<ProtocoloDto>?)

@Keep
data class HistorialEquipoDto(
    val equipo: EquipoDto,
    val totalProtocolos: Int,
    val porResultado: Map<String, Long>?,
    val protocolos: List<ProtocoloDto>?
)
