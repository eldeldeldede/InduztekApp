package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.LoginUiState
import cl.induztek.app.model.ResultadoLogin
import cl.induztek.app.model.Usuario
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =====================================================================
//  LoginViewModel.kt — igual que FormularioViewModel de MiRegistro:
//    - uiState (StateFlow) que la pantalla observa
//    - una función onXxxCambia por cada campo (borra el error de ese campo al escribir)
//    - onIngresar() valida, consulta la BD y devuelve el Usuario (o null si algo falló)
//  Es AndroidViewModel porque InduztekDbHelper necesita el Context (la Application).
// =====================================================================
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null) }
    }

    fun onClaveCambia(valor: String) {
        _uiState.update { it.copy(clave = valor, errorClave = null, errorGeneral = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    // Devuelve el usuario si las credenciales son correctas; si no, null y deja los errores en el uiState.
    fun onIngresar(): Usuario? {
        val estado = _uiState.value

        // 1) Validación de los campos (antes de tocar la base de datos)
        val errorCorreo = ValidacionesFormulario.correo(estado.correo)
        val errorClave = ValidacionesFormulario.clave(estado.clave)
        if (errorCorreo != null || errorClave != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave, errorGeneral = null) }
            return null
        }

        // 2) Consulta a SQLite (compara el HASH de la clave, nunca el texto)
        return when (val resultado = db.iniciarSesion(estado.correo, estado.clave)) {
            is ResultadoLogin.Exito -> {
                _uiState.update { it.copy(clave = "", errorGeneral = null) }
                resultado.usuario
            }
            ResultadoLogin.CredencialesInvalidas -> {
                _uiState.update { it.copy(errorGeneral = "Correo o contraseña incorrectos") }
                null
            }
            ResultadoLogin.CuentaInactiva -> {
                _uiState.update { it.copy(errorGeneral = "Tu cuenta está desactivada. Contacta al Jefe técnico.") }
                null
            }
        }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
