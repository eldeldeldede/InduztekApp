package cl.induztek.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.ReporteActa
import cl.induztek.app.model.ReportesUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  ReportesViewModel.kt — lista de reportes de actas con su seguimiento
//   - Cliente: ve sus reportes y las respuestas.
//   - Técnico: ve todos (para corregir los protocolos observados), sin responder.
//   - Ingeniero y Jefe técnico: ven todos y los responden (En revisión / Resuelto / Rechazado).
// =====================================================================
class ReportesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null

    fun cargar(usuario: Usuario) {
        this.usuario = usuario
        _uiState.update {
            it.copy(
                cargando = true, error = null, esCliente = usuario.esCliente,
                puedeResponder = usuario.codigoRol == CodigoRol.INGENIERO || usuario.codigoRol == CodigoRol.JEFE_TECNICO
            )
        }
        viewModelScope.launch {
            val r = Repositorios.reportes.listar(usuario.idUsuario)
            val nombres = if (usuario.esCliente) emptyMap() else Repositorios.usuarios.nombres()
            _uiState.update {
                it.copy(
                    cargando = false,
                    reportes = (r as? ResultadoRed.Exito)?.datos ?: it.reportes,
                    nombresClientes = nombres,
                    error = r.mensajeError()
                )
            }
        }
    }

    fun onFiltrar(estado: String?) = _uiState.update { it.copy(filtroEstado = estado) }

    // ---------- Diálogo de respuesta ----------
    fun onResponder(reporte: ReporteActa) = _uiState.update {
        it.copy(
            reporteEnRespuesta = reporte,
            estadoRespuesta = if (reporte.estado == "ABIERTO") "EN_REVISION" else "RESUELTO",
            textoRespuesta = reporte.respuesta ?: "",
            errorRespuesta = null, mensaje = null
        )
    }
    fun onEstadoRespuestaCambia(estado: String) = _uiState.update { it.copy(estadoRespuesta = estado, errorRespuesta = null) }
    fun onTextoRespuestaCambia(v: String) = _uiState.update { it.copy(textoRespuesta = v, errorRespuesta = null) }
    fun onCancelarRespuesta() = _uiState.update { it.copy(reporteEnRespuesta = null, errorRespuesta = null) }

    fun onEnviarRespuesta() {
        val u = usuario ?: return
        val e = _uiState.value
        val reporte = e.reporteEnRespuesta ?: return
        if (e.enviandoRespuesta) return
        // Misma regla del servidor: para cerrar el reporte (Resuelto/Rechazado) hay que escribir una respuesta
        if (e.estadoRespuesta != "EN_REVISION" && e.textoRespuesta.isBlank()) {
            _uiState.update { it.copy(errorRespuesta = "Escribe la respuesta para el cliente") }
            return
        }
        _uiState.update { it.copy(enviandoRespuesta = true, errorRespuesta = null) }
        viewModelScope.launch {
            when (val r = Repositorios.reportes.responder(
                u.idUsuario, reporte.idReporte, e.estadoRespuesta, e.textoRespuesta.trim().ifEmpty { null }
            )) {
                is ResultadoRed.Exito -> {
                    _uiState.update {
                        it.copy(
                            enviandoRespuesta = false, reporteEnRespuesta = null,
                            reportes = it.reportes.map { x -> if (x.idReporte == r.datos.idReporte) r.datos else x },
                            mensaje = "Reporte actualizado: ${r.datos.etiquetaEstado}"
                        )
                    }
                }
                else -> _uiState.update { it.copy(enviandoRespuesta = false, errorRespuesta = r.mensajeError()) }
            }
        }
    }
}
