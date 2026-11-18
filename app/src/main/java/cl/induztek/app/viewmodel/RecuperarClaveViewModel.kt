package cl.induztek.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.PasoRecuperacion
import cl.induztek.app.model.RecuperarClaveUiState
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  RecuperarClaveViewModel.kt — "Olvidé mi contraseña" (solo con conexión)
//   Paso 1: se escribe el correo y el servidor envía un código de 6 dígitos por correo.
//   Paso 2: se escribe el código y la nueva contraseña; el servidor la cambia.
//  El código vence y solo sirve una vez: esa regla la controla el servidor, no la app.
// =====================================================================
class RecuperarClaveViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarClaveUiState())
    val uiState: StateFlow<RecuperarClaveUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(v: String) = _uiState.update { it.copy(correo = v.trim(), errorCorreo = null, errorGeneral = null) }
    fun onCodigoCambia(v: String) = _uiState.update { it.copy(codigo = v.trim(), errorCodigo = null, errorGeneral = null) }
    fun onNuevaCambia(v: String) = _uiState.update { it.copy(nueva = v, errorNueva = null, errorGeneral = null) }
    fun onConfirmacionCambia(v: String) = _uiState.update { it.copy(confirmacion = v, errorConfirmacion = null, errorGeneral = null) }
    fun onAlternarVisibilidad() = _uiState.update { it.copy(clavesVisibles = !it.clavesVisibles) }

    // Volver al paso 1 (por si se equivocó de correo o el código venció)
    fun onPedirOtroCodigo() = _uiState.update {
        it.copy(paso = PasoRecuperacion.SOLICITAR, codigo = "", nueva = "", confirmacion = "", errorGeneral = null)
    }

    fun onSolicitarCodigo() {
        val e = _uiState.value
        if (e.cargando) return
        val errorCorreo = ValidacionesFormulario.correo(e.correo)
        if (errorCorreo != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo) }
            return
        }
        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val r = Repositorios.usuarios.solicitarRecuperacion(e.correo)) {
                is ResultadoRed.Exito -> _uiState.update {
                    it.copy(cargando = false, paso = PasoRecuperacion.CONFIRMAR, informacion = r.datos)
                }
                else -> _uiState.update { it.copy(cargando = false, errorGeneral = r.mensajeError()) }
            }
        }
    }

    // `alExito` se llama cuando la contraseña ya cambió (la pantalla vuelve al login)
    fun onConfirmar(alExito: () -> Unit) {
        val e = _uiState.value
        if (e.cargando) return
        val errorCodigo = ValidacionesFormulario.codigoRecuperacion(e.codigo)
        val errorNueva = ValidacionesFormulario.claveNueva(e.nueva, "")
        val errorConfirmacion = ValidacionesFormulario.confirmacionClave(e.nueva, e.confirmacion)
        if (errorCodigo != null || errorNueva != null || errorConfirmacion != null) {
            _uiState.update {
                it.copy(errorCodigo = errorCodigo, errorNueva = errorNueva, errorConfirmacion = errorConfirmacion)
            }
            return
        }
        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val r = Repositorios.usuarios.confirmarRecuperacion(e.correo, e.codigo, e.nueva)) {
                is ResultadoRed.Exito -> {
                    _uiState.update { it.copy(cargando = false, nueva = "", confirmacion = "", codigo = "") }
                    alExito()
                }
                else -> _uiState.update { it.copy(cargando = false, errorGeneral = r.mensajeError()) }
            }
        }
    }
}
