package cl.induztek.app.model

// Reportes de UNA acta, vistos por el cliente dentro de la pantalla del acta.
data class ReportesActaUiState(
    val reportes: List<ReporteActa> = emptyList(),
    val texto: String = "",
    // Protocolo que el cliente observa (opcional). Si estaba formalizado, el servidor lo devuelve a "observado".
    val idProtocolo: Long? = null,
    val enviando: Boolean = false,
    val errorTexto: String? = null,
    val errorGeneral: String? = null,
    val exito: String? = null
)

// Lista general de reportes (menú): el cliente ve los suyos; el personal ve todos y el Ingeniero/Jefe los responde.
data class ReportesUiState(
    val reportes: List<ReporteActa> = emptyList(),
    val nombresClientes: Map<Long, String> = emptyMap(),
    val filtroEstado: String? = null,          // null = todos
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val puedeResponder: Boolean = false,
    val esCliente: Boolean = false,
    // Diálogo "Responder reporte"
    val reporteEnRespuesta: ReporteActa? = null,
    val estadoRespuesta: String = "EN_REVISION",
    val textoRespuesta: String = "",
    val errorRespuesta: String? = null,
    val enviandoRespuesta: Boolean = false
) {
    val visibles: List<ReporteActa>
        get() = if (filtroEstado == null) reportes else reportes.filter { it.estado == filtroEstado }
}
