package cl.induztek.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.CrearUsuarioUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ApiClient
import cl.induztek.app.network.CrearUsuarioSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.RolDto
import cl.induztek.app.network.llamarApi
import cl.induztek.app.validacion.ValidacionesFormulario
import cl.induztek.app.validacion.ValidadorRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  CrearUsuarioViewModel.kt — el Jefe técnico crea cuentas (POST /api/usuarios)
//  El servidor genera la contraseña temporal y la envía al correo del nuevo usuario
//  (por ahora simulado: aparece en la consola de usuarios-service como "[CORREO SIMULADO]").
// =====================================================================
class CrearUsuarioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CrearUsuarioUiState())
    val uiState: StateFlow<CrearUsuarioUiState> = _uiState.asStateFlow()

    init {
        cargarRoles()
    }

    // Pide los roles al servidor; si no hay conexión se quedan los 4 roles locales del caso.
    private fun cargarRoles() {
        viewModelScope.launch {
            val r = llamarApi { ApiClient.usuarios.roles() }
            if (r is ResultadoRed.Exito && r.datos.isNotEmpty()) {
                _uiState.update { it.copy(roles = r.datos) }
            }
        }
    }

    fun onRutCambia(v: String) = _uiState.update { it.copy(rut = v, errorRut = null, errorGeneral = null, exito = null) }
    fun onNombreCambia(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null, errorGeneral = null, exito = null) }
    fun onCorreoCambia(v: String) = _uiState.update { it.copy(correo = v.trim(), errorCorreo = null, errorGeneral = null, exito = null) }
    fun onTelefonoCambia(v: String) = _uiState.update { it.copy(telefono = v, errorTelefono = null, errorGeneral = null, exito = null) }
    fun onRolCambia(v: RolDto) = _uiState.update { it.copy(rol = v, errorRol = null, errorGeneral = null, exito = null) }
    fun onGiroCambia(v: String) = _uiState.update { it.copy(giro = v) }
    fun onContactoNombreCambia(v: String) = _uiState.update { it.copy(contactoNombre = v) }
    fun onContactoCargoCambia(v: String) = _uiState.update { it.copy(contactoCargo = v) }

    fun onLimpiar() = _uiState.update { CrearUsuarioUiState(roles = it.roles) }

    fun onCrear(jefe: Usuario) {
        val e = _uiState.value
        if (e.cargando) return

        val errorRut = ValidadorRut.error(e.rut)
        val errorNombre = ValidacionesFormulario.nombreUsuario(e.nombre)
        val errorCorreo = ValidacionesFormulario.correo(e.correo)
        val errorTelefono = ValidacionesFormulario.telefono(e.telefono)
        val errorRol = if (e.rol == null) "Elige un rol" else null
        if (listOf(errorRut, errorNombre, errorCorreo, errorTelefono, errorRol).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorRut = errorRut, errorNombre = errorNombre, errorCorreo = errorCorreo,
                    errorTelefono = errorTelefono, errorRol = errorRol, exito = null
                )
            }
            return
        }
        val rol = e.rol ?: return

        _uiState.update { it.copy(cargando = true, errorGeneral = null, exito = null) }
        viewModelScope.launch {
            val solicitud = CrearUsuarioSolicitud(
                rut = ValidadorRut.formatear(e.rut),
                nombre = e.nombre.trim(),
                correo = e.correo,
                telefono = e.telefono.trim().ifBlank { null },
                rolId = rol.id,
                giro = if (e.esCliente) e.giro.trim().ifBlank { null } else null,
                contactoNombre = if (e.esCliente) e.contactoNombre.trim().ifBlank { null } else null,
                contactoCargo = if (e.esCliente) e.contactoCargo.trim().ifBlank { null } else null
            )
            when (val r = llamarApi { ApiClient.usuarios.crear(jefe.idUsuario, solicitud) }) {
                is ResultadoRed.Exito -> _uiState.update {
                    CrearUsuarioUiState(
                        roles = it.roles,
                        exito = "Usuario creado. Se envió una contraseña temporal a ${r.datos.correo}."
                    )
                }
                is ResultadoRed.ErrorServidor ->
                    _uiState.update { it.copy(cargando = false, errorGeneral = r.mensaje) }
                ResultadoRed.SinConexion ->
                    _uiState.update {
                        it.copy(cargando = false, errorGeneral = "Sin conexión con el servidor. Crear usuarios requiere internet.")
                    }
            }
        }
    }
}
