package cl.induztek.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.Comuna
import cl.induztek.app.model.InstalacionFormUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.InstalacionSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  InstalacionFormViewModel.kt — el personal crea o edita una instalación de un cliente
//  Requiere conexión (se guarda directo en equipos-service).
// =====================================================================
class InstalacionFormViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InstalacionFormUiState())
    val uiState: StateFlow<InstalacionFormUiState> = _uiState.asStateFlow()

    fun cargar(idInstalacion: Long, usuario: Usuario) {
        _uiState.update { it.copy(idInstalacion = idInstalacion, cargando = true, errorGeneral = null, guardado = false) }
        viewModelScope.launch {
            val comunas = (Repositorios.equipos.comunas() as? ResultadoRed.Exito)?.datos ?: emptyList()
            val clientes = (Repositorios.usuarios.listarClientes() as? ResultadoRed.Exito)?.datos?.filter { it.activo } ?: emptyList()
            _uiState.update { it.copy(comunas = comunas, clientes = clientes) }
            if (idInstalacion != 0L) {
                when (val r = Repositorios.equipos.instalacion(idInstalacion, usuario)) {
                    is ResultadoRed.Exito -> _uiState.update {
                        it.copy(
                            nombre = r.datos.nombre, ubicacion = r.datos.ubicacion,
                            observaciones = r.datos.observacionesAcceso ?: "",
                            comuna = comunas.firstOrNull { c -> c.idComuna == r.datos.idComuna },
                            cliente = clientes.firstOrNull { c -> c.idUsuario == r.datos.idUsuario },
                            clienteNombreFijo = r.datos.clienteNombre
                        )
                    }
                    else -> _uiState.update { it.copy(errorGeneral = r.mensajeError()) }
                }
            }
            _uiState.update { it.copy(cargando = false) }
        }
    }

    fun onClienteCambia(v: Usuario) = _uiState.update { it.copy(cliente = v, errorCliente = null, errorGeneral = null) }
    fun onNombreCambia(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null, errorGeneral = null) }
    fun onUbicacionCambia(v: String) = _uiState.update { it.copy(ubicacion = v) }
    fun onComunaCambia(v: Comuna) = _uiState.update { it.copy(comuna = v, errorComuna = null, errorGeneral = null) }
    fun onObservacionesCambia(v: String) = _uiState.update { it.copy(observaciones = v) }

    fun onGuardar() {
        val e = _uiState.value
        if (e.guardando) return
        val errorCliente = if (e.esNueva && e.cliente == null) "Elige el cliente" else null
        val errorNombre = if (e.nombre.trim().length < 3) "Ingresa el nombre (mínimo 3 caracteres)" else null
        val errorComuna = if (e.comuna == null) "Elige la comuna" else null
        if (errorCliente != null || errorNombre != null || errorComuna != null) {
            _uiState.update { it.copy(errorCliente = errorCliente, errorNombre = errorNombre, errorComuna = errorComuna) }
            return
        }
        val comuna = e.comuna ?: return
        _uiState.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            val solicitud = InstalacionSolicitud(
                clienteId = e.cliente?.idUsuario,
                nombre = e.nombre.trim(),
                ubicacion = e.ubicacion.trim().ifBlank { null },
                comunaId = comuna.idComuna,
                observacionesAcceso = e.observaciones.trim().ifBlank { null }
            )
            val r = if (e.esNueva) Repositorios.equipos.crearInstalacion(solicitud)
            else Repositorios.equipos.actualizarInstalacion(e.idInstalacion, solicitud)
            when (r) {
                is ResultadoRed.Exito -> _uiState.update { it.copy(guardando = false, guardado = true) }
                else -> _uiState.update { it.copy(guardando = false, errorGeneral = r.mensajeError()) }
            }
        }
    }
}
