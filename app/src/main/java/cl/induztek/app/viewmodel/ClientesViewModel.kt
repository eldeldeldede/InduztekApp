package cl.induztek.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.ClientesUiState
import cl.induztek.app.model.CodigoRol
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
//  ClientesViewModel.kt — el personal ve, edita y (solo el Jefe) desactiva las cuentas de clientes
//  Crear un cliente nuevo se hace en la pantalla "Crear usuario" (con el rol fijo en CLIENTE).
//  Todo requiere conexión: son cuentas del servidor.
// =====================================================================
class ClientesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    private var actor: Usuario? = null

    fun cargar(actor: Usuario) {
        this.actor = actor
        _uiState.update { it.copy(cargando = true, error = null, puedeDesactivar = actor.codigoRol == CodigoRol.JEFE_TECNICO) }
        viewModelScope.launch {
            val r = Repositorios.usuarios.listarClientes()
            _uiState.update {
                it.copy(
                    cargando = false,
                    clientes = (r as? ResultadoRed.Exito)?.datos ?: it.clientes,
                    error = r.mensajeError()
                )
            }
        }
    }

    fun onBuscar(v: String) = _uiState.update { it.copy(filtro = v) }

    // ---------- Activar / desactivar (solo Jefe técnico; el servidor también lo valida) ----------
    fun onCambiarEstado(cliente: Usuario) {
        val a = actor ?: return
        viewModelScope.launch {
            when (val r = Repositorios.usuarios.cambiarEstado(a.idUsuario, cliente.idUsuario, !cliente.activo)) {
                is ResultadoRed.Exito -> _uiState.update {
                    it.copy(
                        clientes = it.clientes.map { c -> if (c.idUsuario == r.datos.idUsuario) r.datos else c },
                        mensaje = if (r.datos.activo) "Cuenta activada" else "Cuenta desactivada", error = null
                    )
                }
                else -> _uiState.update { it.copy(error = r.mensajeError(), mensaje = null) }
            }
        }
    }

    // ---------- Edición ----------
    fun onEditar(c: Usuario) = _uiState.update {
        it.copy(
            clienteEnEdicion = c, nombre = c.nombre, telefono = c.telefono ?: "", giro = c.giro ?: "",
            contactoNombre = c.contactoNombre ?: "", contactoCargo = c.contactoCargo ?: "",
            errorNombre = null, errorTelefono = null, errorEdicion = null, mensaje = null
        )
    }
    fun onNombreCambia(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null) }
    fun onTelefonoCambia(v: String) = _uiState.update { it.copy(telefono = v, errorTelefono = null) }
    fun onGiroCambia(v: String) = _uiState.update { it.copy(giro = v) }
    fun onContactoNombreCambia(v: String) = _uiState.update { it.copy(contactoNombre = v) }
    fun onContactoCargoCambia(v: String) = _uiState.update { it.copy(contactoCargo = v) }
    fun onCancelarEdicion() = _uiState.update { it.copy(clienteEnEdicion = null) }

    fun onGuardarEdicion() {
        val a = actor ?: return
        val e = _uiState.value
        val cliente = e.clienteEnEdicion ?: return
        if (e.guardando) return
        val errorNombre = ValidacionesFormulario.nombreUsuario(e.nombre)
        val errorTelefono = ValidacionesFormulario.telefono(e.telefono)
        if (errorNombre != null || errorTelefono != null) {
            _uiState.update { it.copy(errorNombre = errorNombre, errorTelefono = errorTelefono) }
            return
        }
        _uiState.update { it.copy(guardando = true, errorEdicion = null) }
        viewModelScope.launch {
            // El correo y el RUT no se cambian (son la identidad de la cuenta): el correo va en null
            val solicitud = ActualizarPerfilSolicitud(
                nombre = e.nombre.trim(), correo = null, telefono = e.telefono.trim().ifBlank { null },
                giro = e.giro.trim().ifBlank { null }, contactoNombre = e.contactoNombre.trim().ifBlank { null },
                contactoCargo = e.contactoCargo.trim().ifBlank { null }
            )
            when (val r = Repositorios.usuarios.actualizarPerfil(a.idUsuario, cliente.idUsuario, solicitud)) {
                is ResultadoRed.Exito -> _uiState.update {
                    it.copy(
                        guardando = false, clienteEnEdicion = null, mensaje = "Cliente actualizado",
                        clientes = it.clientes.map { c -> if (c.idUsuario == r.datos.idUsuario) r.datos else c }
                    )
                }
                else -> _uiState.update { it.copy(guardando = false, errorEdicion = r.mensajeError()) }
            }
        }
    }
}
