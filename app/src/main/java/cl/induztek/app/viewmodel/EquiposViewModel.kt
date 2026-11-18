package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.EquiposUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// EquiposViewModel — equipos de una instalación con buscador (datos desde el repositorio).
class EquiposViewModel(application: Application) : AndroidViewModel(application) {

    private var todos: List<Equipo> = emptyList()

    private val _uiState = MutableStateFlow(EquiposUiState())
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    fun cargar(idInstalacion: Long, usuario: Usuario) {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val instalacion = Repositorios.equipos.instalacion(idInstalacion, usuario)
            val equipos = Repositorios.equipos.equipos(idInstalacion, usuario)
            if (equipos is ResultadoRed.Exito) todos = equipos.datos
            _uiState.update {
                it.copy(
                    instalacion = (instalacion as? ResultadoRed.Exito)?.datos,
                    cargando = false,
                    error = instalacion.mensajeError() ?: equipos.mensajeError()
                )
            }
            aplicarFiltro(_uiState.value.filtro)
        }
    }

    fun buscar(texto: String) = aplicarFiltro(texto)

    private fun aplicarFiltro(texto: String) {
        val buscado = texto.trim()
        val lista = if (buscado.isEmpty()) todos else todos.filter {
            it.tag.contains(buscado, ignoreCase = true) ||
                it.tipoEquipoNombre.contains(buscado, ignoreCase = true) ||
                (it.marca ?: "").contains(buscado, ignoreCase = true)
        }
        _uiState.update { it.copy(filtro = texto, equipos = lista) }
    }
}
