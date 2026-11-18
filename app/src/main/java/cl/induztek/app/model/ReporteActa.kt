package cl.induztek.app.model

// =====================================================================
//  ReporteActa.kt — un reporte que el cliente hace sobre un acta (y su seguimiento)
//  estado: ABIERTO -> EN_REVISION -> RESUELTO o RECHAZADO (lo cambia el Ingeniero/Jefe con una respuesta)
// =====================================================================
data class ReporteActa(
    val idReporte: Long,
    val idVisita: Long,
    val numeroOt: String,
    val idProtocolo: Long?,
    val numeroProtocolo: String?,
    val idCliente: Long,
    val texto: String,
    val estado: String,
    val respuesta: String?,
    val fechaHoraCreacion: String,
    val fechaHoraActualizacion: String
) {
    val etiquetaEstado: String
        get() = when (estado) {
            "ABIERTO" -> "Abierto"
            "EN_REVISION" -> "En revisión"
            "RESUELTO" -> "Resuelto"
            "RECHAZADO" -> "Rechazado"
            else -> estado
        }

    val estaCerrado: Boolean get() = estado == "RESUELTO" || estado == "RECHAZADO"
}
