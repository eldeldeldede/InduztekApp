package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.data.SesionPreferencias
import cl.induztek.app.model.PerfilUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ActualizarPerfilSolicitud
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
//  PerfilViewModel.kt — "Mi perfil": cada usuario edita sus propios datos (PUT /api/usuarios/{id})
//  Editable: nombre y teléfono; las empresas cliente además giro y contacto.
//  NO editable: RUT, correo y rol (los define el Jefe técnico al crear la cuenta).
// =====================================================================
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // Se llena una sola vez con los datos de la sesión (si la pantalla se recompone no se pierde lo escrito)
    fun cargar(usuario: Usuario) {
        if (_uiState.value.cargado) return
        _uiState.value = PerfilUiState(
            rut = usuario.rut,
            correo = usuario.correo,
            rolEtiqueta = usuario.codigoRol.etiqueta,
            esCliente = usuario.esCliente,
            nombre = usuario.nombre,
            telefono = usuario.telefono ?: "",
            giro = usuario.giro ?: "",
            contactoNombre = usuario.contactoNombre ?: "",
            contactoCargo = usuario.contactoCargo ?: "",
            cargado = true
        )
    }

    fun onNombreCambia(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null, errorGeneral = null, exito = null) }
    fun onTelefonoCambia(v: String) = _uiState.update { it.copy(telefono = v, errorTelefono = null, errorGeneral = null, exito = null) }
    fun onGiroCambia(v: String) = _uiState.update { it.copy(giro = v, exito = null) }
    fun onContactoNombreCambia(v: String) = _uiState.update { it.copy(contactoNombre = v, exito = null) }
    fun onContactoCargoCambia(v: String) = _uiState.update { it.copy(contactoCargo = v, exito = null) }

    // `alExito` recibe el usuario actualizado para que la sesión (menú, saludo) muestre el nombre nuevo
    fun onGuardar(usuario: Usuario, alExito: (Usuario) -> Unit) {
        val e = _uiState.value
        if (e.guardando) return
        val errorNombre = ValidacionesFormulario.nombreUsuario(e.nombre)
        val errorTelefono = ValidacionesFormulario.telefono(e.telefono)
        if (errorNombre != null || errorTelefono != null) {
            _uiState.update { it.copy(errorNombre = errorNombre, errorTelefono = errorTelefono, exito = null) }
            return
        }
        _uiState.update { it.copy(guardando = true, errorGeneral = null, exito = null) }
        viewModelScope.launch {
            val solicitud = ActualizarPerfilSolicitud(
                nombre = e.nombre.trim(),
                correo = null,
                telefono = e.telefono.trim().ifEmpty { null },
                giro = if (e.esCliente) e.giro.trim().ifEmpty { null } else null,
                contactoNombre = if (e.esCliente) e.contactoNombre.trim().ifEmpty { null } else null,
                contactoCargo = if (e.esCliente) e.contactoCargo.trim().ifEmpty { null } else null
            )
            when (val r = Repositorios.usuarios.actualizarPerfil(usuario.idUsuario, usuario.idUsuario, solicitud)) {
                is ResultadoRed.Exito -> {
                    // Se conserva debeCambiarContrasena y se refresca el "último ingreso" usado sin conexión
                    sesion.actualizarUltimoIngreso(r.datos)
                    _uiState.update { it.copy(guardando = false, exito = "Perfil actualizado") }
                    alExito(r.datos)
                }
                else -> _uiState.update { it.copy(guardando = false, errorGeneral = r.mensajeError()) }
            }
        }
    }
}
