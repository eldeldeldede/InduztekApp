package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.data.Seguridad
import cl.induztek.app.data.SesionPreferencias
import cl.induztek.app.model.CambiarClaveUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  CambiarClaveViewModel.kt — cambio de contraseña (PUT /api/usuarios/{id}/contrasena)
//  Un usuario nuevo entra con la clave TEMPORAL que le llegó por correo y la app lo obliga a
//  cambiarla antes de seguir (regla del profesor: el Jefe no define la contraseña de nadie).
// =====================================================================
class CambiarClaveViewModel(application: Application) : AndroidViewModel(application) {

    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(CambiarClaveUiState())
    val uiState: StateFlow<CambiarClaveUiState> = _uiState.asStateFlow()

    fun onActualCambia(v: String) = _uiState.update { it.copy(actual = v, errorActual = null, errorGeneral = null) }
    fun onNuevaCambia(v: String) = _uiState.update { it.copy(nueva = v, errorNueva = null, errorGeneral = null) }
    fun onConfirmacionCambia(v: String) = _uiState.update { it.copy(confirmacion = v, errorConfirmacion = null, errorGeneral = null) }
    fun onAlternarVisibilidad() = _uiState.update { it.copy(clavesVisibles = !it.clavesVisibles) }

    // `alExito` recibe el usuario ya actualizado (debeCambiarContrasena = false)
    fun onGuardar(usuario: Usuario, alExito: (Usuario) -> Unit) {
        val e = _uiState.value
        if (e.cargando) return

        val errorActual = ValidacionesFormulario.clave(e.actual)
        val errorNueva = ValidacionesFormulario.claveNueva(e.nueva, e.actual)
        val errorConfirmacion = ValidacionesFormulario.confirmacionClave(e.nueva, e.confirmacion)
        if (errorActual != null || errorNueva != null || errorConfirmacion != null) {
            _uiState.update {
                it.copy(errorActual = errorActual, errorNueva = errorNueva, errorConfirmacion = errorConfirmacion)
            }
            return
        }

        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val r = Repositorios.usuarios.cambiarContrasena(usuario.idUsuario, e.actual, e.nueva)) {
                is ResultadoRed.Exito -> {
                    val actualizado = usuario.copy(debeCambiarContrasena = false)
                    // La clave nueva reemplaza a la guardada para el ingreso sin conexión
                    sesion.guardarUltimoIngreso(actualizado, Seguridad.sha256(e.nueva))
                    _uiState.update { it.copy(cargando = false, actual = "", nueva = "", confirmacion = "") }
                    alExito(actualizado)
                }
                is ResultadoRed.ErrorServidor ->
                    _uiState.update { it.copy(cargando = false, errorGeneral = r.mensaje) }
                ResultadoRed.SinConexion ->
                    _uiState.update {
                        it.copy(cargando = false, errorGeneral = "Sin conexión con el servidor. Conéctate a internet para cambiar tu contraseña.")
                    }
            }
        }
    }
}
