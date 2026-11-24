package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.ActasUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ActasViewModel — visitas (actas) de una instalación. Al cliente solo le llegan las de su empresa.
class ActasViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ActasUiState())
    val uiState: StateFlow<ActasUiState> = _uiState.asStateFlow()

    fun cargar(idInstalacion: Long, usuario: Usuario) {
        _uiState.value = _uiState.value.copy(cargando = true, error = null, esCliente = usuario.esCliente)
        viewModelScope.launch {
            val instalacion = Repositorios.equipos.instalacion(idInstalacion, usuario)
            val visitas = Repositorios.protocolos.visitas(idInstalacion, usuario)
            val estados = Repositorios.protocolos.estadosDeActas(idInstalacion, usuario)
            val lista = (visitas as? ResultadoRed.Exito)?.datos ?: emptyList()
            _uiState.value = ActasUiState(
                cargado = true,
                instalacion = (instalacion as? ResultadoRed.Exito)?.datos,
                visitas = lista.sortedByDescending { estados[it.idVisita]?.pendiente == true },
                estados = estados,
                esCliente = usuario.esCliente,
                cargando = false,
                error = instalacion.mensajeError() ?: visitas.mensajeError()
            )
        }
    }
}
