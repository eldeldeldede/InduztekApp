package cl.induztek.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.ReportesActaUiState
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
//  ReportesActaViewModel.kt — el CLIENTE reporta un problema de un acta y ve su seguimiento
//  Solo con conexión. El reporte parte "Abierto"; el Ingeniero o el Jefe técnico lo pasan a
//  "En revisión" y luego a "Resuelto" o "Rechazado" con una respuesta escrita.
//  Si el cliente indica un protocolo ya formalizado, el servidor lo devuelve a "observado"
//  para que el técnico lo corrija y el ingeniero lo formalice de nuevo.
// =====================================================================
class ReportesActaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportesActaUiState())
    val uiState: StateFlow<ReportesActaUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var idVisita: Long = 0

    fun cargar(idVisita: Long, usuario: Usuario) {
        this.idVisita = idVisita
        this.usuario = usuario
        viewModelScope.launch { refrescar() }
    }

    private suspend fun refrescar() {
        val u = usuario ?: return
        when (val r = Repositorios.reportes.listar(u.idUsuario, idVisita)) {
            is ResultadoRed.Exito -> _uiState.update { it.copy(reportes = r.datos) }
            else -> _uiState.update { it.copy(errorGeneral = r.mensajeError()) }
        }
    }

    fun onTextoCambia(v: String) = _uiState.update { it.copy(texto = v, errorTexto = null, errorGeneral = null, exito = null) }
    fun onProtocoloSeleccionado(id: Long?) = _uiState.update { it.copy(idProtocolo = id, exito = null) }

    // `alEnviado` permite refrescar el acta: si el reporte devolvió un protocolo a "observado", cambia su estado
    fun onEnviar(alEnviado: () -> Unit) {
        val u = usuario ?: return
        val e = _uiState.value
        if (e.enviando) return
        if (e.texto.trim().length < 10) {
            _uiState.update { it.copy(errorTexto = "Describe el problema (mínimo 10 caracteres)") }
            return
        }
        _uiState.update { it.copy(enviando = true, errorGeneral = null, exito = null) }
        viewModelScope.launch {
            when (val r = Repositorios.reportes.crear(u.idUsuario, idVisita, e.idProtocolo, e.texto.trim())) {
                is ResultadoRed.Exito -> {
                    _uiState.update {
                        it.copy(enviando = false, texto = "", idProtocolo = null,
                            exito = "Reporte enviado. El equipo de Induztek lo revisará y verás aquí la respuesta.")
                    }
                    refrescar()
                    alEnviado()
                }
                else -> _uiState.update { it.copy(enviando = false, errorGeneral = r.mensajeError()) }
            }
        }
    }
}
