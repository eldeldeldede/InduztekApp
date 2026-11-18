package cl.induztek.app.local

import cl.induztek.app.model.Parametro
import cl.induztek.app.model.TipoPrueba
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.network.HistorialDto
import cl.induztek.app.network.MedicionDto
import cl.induztek.app.network.ProtocoloDto
import cl.induztek.app.network.PruebaDto
import cl.induztek.app.network.PruebaSolicitud
import cl.induztek.app.network.VisitaDto

// =====================================================================
//  ProtocoloLocal.kt — cómo se ve un protocolo/visita mientras sus cambios esperan conexión
//  ---------------------------------------------------------------------
//  Son funciones puras (reciben un dato y devuelven uno nuevo): copian el DTO del servidor aplicando
//  el cambio hecho sin conexión. Así la pantalla muestra lo que el técnico acaba de hacer.
// =====================================================================

// Datos que la pantalla ya calculó y que se necesitan para mostrar una prueba antes de que el servidor la evalúe.
data class ContextoPruebaLocal(
    val tipo: TipoPrueba,
    val parametros: List<Parametro>,
    val resultado: String?
)

fun nuevoProtocoloLocal(
    id: Long,
    idVisita: Long,
    numeroOt: String?,
    idEquipo: Long,
    idActor: Long,
    ahora: String
): ProtocoloDto = ProtocoloDto(
    id = id,
    numero = "PEND-${-id}",
    visitaId = idVisita,
    visitaNumeroOt = numeroOt,
    equipoId = idEquipo,
    usuarioRegistraId = idActor,
    fechaHoraPrueba = ahora,
    temperaturaC = null,
    humedadPct = null,
    descripcionMantenimiento = null,
    observaciones = null,
    estado = "BORRADOR",
    protocoloReemplazadoId = null,
    resultadoGlobal = null,
    atrasado = false,
    pruebas = emptyList(),
    historial = emptyList()
)

fun ProtocoloDto.conCondiciones(temperaturaC: Double, humedadPct: Double): ProtocoloDto =
    copy(temperaturaC = temperaturaC, humedadPct = humedadPct)

fun ProtocoloDto.conPrueba(prueba: PruebaDto): ProtocoloDto = copy(pruebas = (pruebas ?: emptyList()) + prueba)

fun ProtocoloDto.sinPrueba(idPrueba: Long): ProtocoloDto =
    copy(pruebas = (pruebas ?: emptyList()).filter { it.id != idPrueba })

// Resultado global = el peor entre las pruebas realizadas (Aprobado < Observado < Rechazado).
fun ProtocoloDto.resultadoGlobalCalculado(): String? =
    (pruebas ?: emptyList()).mapNotNull { it.resultado }.map { TipoResultado.valueOf(it) }.maxOrNull()?.name

fun ProtocoloDto.registradoEnTerreno(idActor: Long, ahora: String, idHistorial: Long): ProtocoloDto = copy(
    estado = "REGISTRADO_TERRENO",
    resultadoGlobal = resultadoGlobalCalculado(),
    historial = (historial ?: emptyList()) + HistorialDto(idHistorial, estado, "REGISTRADO_TERRENO", idActor, ahora, null)
)

fun VisitaDto.cerradaLocal(ahora: String): VisitaDto = copy(cerrada = true, fechaHoraCierre = ahora)

fun pruebaLocal(id: Long, solicitud: PruebaSolicitud, contexto: ContextoPruebaLocal): PruebaDto = PruebaDto(
    id = id,
    tipoPruebaId = solicitud.tipoPruebaId,
    tipoPruebaCodigo = contexto.tipo.codigo,
    tipoPruebaNombre = contexto.tipo.nombre,
    elemento = solicitud.elemento,
    momento = solicitud.momento,
    realizada = solicitud.realizada,
    motivoNoRealizada = solicitud.motivoNoRealizada,
    instrumentoId = solicitud.instrumentoId,
    resultado = if (solicitud.realizada) contexto.resultado else null,
    valorCalculado = null,
    comentario = solicitud.comentario,
    mediciones = (solicitud.mediciones ?: emptyList()).map { m ->
        val parametro = contexto.parametros.firstOrNull { it.codigo == m.parametro }
        MedicionDto(m.parametro, parametro?.nombre, parametro?.unidad, m.valor)
    }
)
