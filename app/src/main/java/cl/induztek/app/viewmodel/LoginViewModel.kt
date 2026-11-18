package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.data.Seguridad
import cl.induztek.app.data.SesionPreferencias
import cl.induztek.app.model.LoginUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ApiClient
import cl.induztek.app.network.LoginSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.llamarApi
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  LoginViewModel.kt — igual que FormularioViewModel de MiRegistro, pero ahora el login lo valida
//  el SERVIDOR (POST /api/usuarios/login en usuarios-service).
//
//   1) Valida los campos (sin tocar la red).
//   2) Llama al servidor en una corrutina (viewModelScope.launch): la pantalla no se congela.
//   3) Si el servidor responde bien -> guarda "último ingreso" (usuario + hash de la clave) y avisa.
//   4) Si NO hay conexión -> modo terreno: entra solo si coincide con el último ingreso guardado
//      en este teléfono (RNF-02).
// =====================================================================
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sesion = SesionPreferencias(application)

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

    // `alExito` recibe el usuario cuando el ingreso fue correcto (la pantalla lo pasa a la navegación).
    fun onIngresar(alExito: (Usuario) -> Unit) {
        val estado = _uiState.value
        if (estado.cargando) return

        val errorCorreo = ValidacionesFormulario.correo(estado.correo)
        val errorClave = ValidacionesFormulario.clave(estado.clave)
        if (errorCorreo != null || errorClave != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave, errorGeneral = null) }
            return
        }

        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val resultado = llamarApi { ApiClient.usuarios.login(LoginSolicitud(estado.correo, estado.clave)) }) {
                is ResultadoRed.Exito -> {
                    val usuario = resultado.datos.aUsuario()
                    if (!usuario.activo) {
                        terminar("Tu cuenta está desactivada. Contacta al Jefe técnico.")
                    } else {
                        sesion.guardarUltimoIngreso(usuario, Seguridad.sha256(estado.clave))
                        _uiState.update { it.copy(cargando = false, clave = "") }
                        alExito(usuario)
                    }
                }
                is ResultadoRed.ErrorServidor -> terminar(resultado.mensaje)
                ResultadoRed.SinConexion -> ingresarSinConexion(estado, alExito)
            }
        }
    }

    // Modo terreno: no hay servidor, se acepta solo al último usuario que ya entró en este teléfono.
    private fun ingresarSinConexion(estado: LoginUiState, alExito: (Usuario) -> Unit) {
        val ultimo = sesion.leerUltimoIngreso()
        if (ultimo == null || !ultimo.usuario.correo.equals(estado.correo, ignoreCase = true)) {
            terminar("Sin conexión con el servidor. Para ingresar sin señal debes haber iniciado sesión antes en este teléfono.")
            return
        }
        if (ultimo.hashClave != Seguridad.sha256(estado.clave)) {
            terminar("Correo o contraseña incorrectos")
            return
        }
        if (ultimo.usuario.debeCambiarContrasena) {
            terminar("Debes cambiar tu contraseña temporal con conexión a internet.")
            return
        }
        _uiState.update { it.copy(cargando = false, clave = "") }
        alExito(ultimo.usuario)
    }

    private fun terminar(mensaje: String) {
        _uiState.update { it.copy(cargando = false, errorGeneral = mensaje) }
    }
}
