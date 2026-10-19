package cl.induztek.app.model

import java.time.LocalDateTime

data class HistorialEstado(
    val idHistorial: Long,
    val idProtocolo: Long,
    val idEstadoAnterior: Long,
    val idEstadoNuevo: Long,
    val idUsuario: Long,
    val fechaHora: LocalDateTime,
    val motivo: String?
)


