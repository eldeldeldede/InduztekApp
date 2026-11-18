package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.Instalacion
import cl.induztek.app.model.InstalacionesUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  InstalacionesViewModel — lista de instalaciones con buscador
//  Los datos llegan desde el repositorio (que consulta equipos-service); aquí solo se filtran y se exponen.
// =====================================================================
class InstalacionesViewModel(application: Application) : AndroidViewModel(application) {

    private var todas: List<Instalacion> = emptyList()

    private val _uiState = MutableStateFlow(InstalacionesUiState())
    val uiState: StateFlow<InstalacionesUiState> = _uiState.asStateFlow()

    fun cargar(usuarioActual: Usuario) {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val resultado = Repositorios.equipos.instalaciones(usuarioActual)
            if (resultado is ResultadoRed.Exito) todas = resultado.datos
            _uiState.update { it.copy(cargando = false, error = resultado.mensajeError()) }
            aplicarFiltro(_uiState.value.filtro)
        }
    }

    fun buscar(texto: String) = aplicarFiltro(texto)

    private fun aplicarFiltro(texto: String) {
        val buscado = texto.trim()
        val lista = if (buscado.isEmpty()) todas else todas.filter {
            it.nombre.contains(buscado, ignoreCase = true) ||
                it.comunaNombre.contains(buscado, ignoreCase = true) ||
                it.clienteNombre.contains(buscado, ignoreCase = true)
        }
        _uiState.update { it.copy(filtro = texto, instalaciones = lista) }
    }
}
