package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.EquiposUiState
import cl.induztek.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EquiposViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)
    private var todos: List<Equipo> = emptyList()

    private val _uiState = MutableStateFlow(EquiposUiState())
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    fun cargar(idInstalacion: Long, usuario: Usuario) {

        val instalacion = db.buscarInstalacion(idInstalacion, usuario)
        todos = if (instalacion == null) emptyList() else db.listarEquipos(idInstalacion, usuario)
        _uiState.update { it.copy(instalacion = instalacion) }
        aplicarFiltro(_uiState.value.filtro)
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

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
