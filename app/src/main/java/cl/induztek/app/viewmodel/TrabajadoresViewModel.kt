package cl.induztek.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.TrabajadoresUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El Jefe técnico ve los perfiles del personal y puede activar o desactivar sus cuentas.
// Son cuentas del servidor: requiere conexión.
class TrabajadoresViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TrabajadoresUiState())
    val uiState: StateFlow<TrabajadoresUiState> = _uiState.asStateFlow()

    fun cargar(actor: Usuario) {
        _uiState.update { it.copy(cargando = true, error = null, idActor = actor.idUsuario) }
        viewModelScope.launch {
            val r = Repositorios.usuarios.listarPersonal()
            _uiState.update {
                it.copy(
                    cargando = false,
                    trabajadores = (r as? ResultadoRed.Exito)?.datos ?: it.trabajadores,
                    error = r.mensajeError()
                )
            }
        }
    }

    fun onBuscar(v: String) = _uiState.update { it.copy(filtro = v) }

    fun onVerPerfil(u: Usuario) = _uiState.update { it.copy(seleccionado = u) }

    fun onCerrarPerfil() = _uiState.update { it.copy(seleccionado = null) }

    fun onCambiarEstado(trabajador: Usuario) {
        val actor = _uiState.value.idActor
        viewModelScope.launch {
            when (val r = Repositorios.usuarios.cambiarEstado(actor, trabajador.idUsuario, !trabajador.activo)) {
                is ResultadoRed.Exito -> _uiState.update {
                    it.copy(
                        trabajadores = it.trabajadores.map { t -> if (t.idUsuario == r.datos.idUsuario) r.datos else t },
                        seleccionado = it.seleccionado?.let { s -> if (s.idUsuario == r.datos.idUsuario) r.datos else s },
                        mensaje = if (r.datos.activo) "Cuenta activada" else "Cuenta desactivada",
                        error = null
                    )
                }
                else -> _uiState.update { it.copy(error = r.mensajeError(), mensaje = null) }
            }
        }
    }
}
