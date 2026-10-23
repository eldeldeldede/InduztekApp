package cl.induztek.app.model

import java.time.LocalDateTime

// Trazabilidad (RNF-07): quién cambió el estado de un protocolo, cuándo y por qué.
data class HistorialEstado(
    val idHistorial: Long = 0,
    val idProtocolo: Long,
    val idEstadoAnterior: Long?, // CORREGIDO: null cuando el protocolo se acaba de crear
    val idEstadoNuevo: Long,
    val idUsuario: Long,
    val fechaHora: LocalDateTime,
    val motivo: String?          // obligatorio para OBSERVADO y ANULADO
)
