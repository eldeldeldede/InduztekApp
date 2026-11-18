package cl.induztek.app.repository

import cl.induztek.app.model.ReporteActa
import cl.induztek.app.network.ApiClient
import cl.induztek.app.network.ReporteActaDto
import cl.induztek.app.network.ReporteActaSolicitud
import cl.induztek.app.network.ResponderReporteSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.llamarApi
import cl.induztek.app.network.mapear

// =====================================================================
//  ReporteRepositorio.kt — reportes del cliente sobre un acta, con su seguimiento
//  El cliente reporta; el Ingeniero o el Jefe técnico responden y cambian el estado.
// =====================================================================
class ReporteRepositorio {

    suspend fun crear(idActor: Long, idVisita: Long, idProtocolo: Long?, texto: String): ResultadoRed<ReporteActa> =
        llamarApi { ApiClient.protocolos.crearReporte(idActor, idVisita, ReporteActaSolicitud(idProtocolo, texto)) }
            .mapear { it.aReporte() }

    suspend fun listar(idActor: Long, idVisita: Long? = null): ResultadoRed<List<ReporteActa>> =
        llamarApi { ApiClient.protocolos.reportes(idActor, idVisita) }.mapear { lista -> lista.map { it.aReporte() } }

    suspend fun responder(idActor: Long, idReporte: Long, estado: String, respuesta: String?): ResultadoRed<ReporteActa> =
        llamarApi {
            ApiClient.protocolos.responderReporte(idActor, idReporte, ResponderReporteSolicitud(estado, respuesta))
        }.mapear { it.aReporte() }

    private fun ReporteActaDto.aReporte(): ReporteActa = ReporteActa(
        idReporte = id,
        idVisita = visitaId,
        numeroOt = numeroOt ?: "",
        idProtocolo = protocoloId,
        numeroProtocolo = protocoloNumero,
        idCliente = clienteId,
        texto = texto,
        estado = estado,
        respuesta = respuesta,
        fechaHoraCreacion = fechaHoraCreacion,
        fechaHoraActualizacion = fechaHoraActualizacion
    )
}
