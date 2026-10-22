package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.Instalacion
import cl.induztek.app.model.InstalacionesUiState
import cl.induztek.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class `InstalacionesViewModel.kt`(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)

    private var todas: List<Instalacion> = emptyList()
    private var usuario: Usuario? = null

    private val _uiState = MutableStateFlow(InstalacionesUiState())
    val uiState: StateFlow<InstalacionesUiState> = _uiState.asStateFlow()

    fun cargar(usuarioActual: Usuario) {
        usuario = usuarioActual
        todas = db.listarInstalaciones(usuarioActual)
        aplicarFiltro(_uiState.value.filtro)
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

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}