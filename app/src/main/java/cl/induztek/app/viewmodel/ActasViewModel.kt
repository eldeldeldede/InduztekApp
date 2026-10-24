package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.ActasUiState
import cl.induztek.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActasViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)

    private val _uiState = MutableStateFlow(ActasUiState())
    val uiState: StateFlow<ActasUiState> = _uiState.asStateFlow()

    fun cargar(idInstalacion: Long, usuario: Usuario) {

        val instalacion = db.buscarInstalacion(idInstalacion, usuario)
        val visitas = if (instalacion == null) emptyList() else db.listarVisitas(idInstalacion, usuario)
        _uiState.value = ActasUiState(
            cargado = true,
            instalacion = instalacion,
            visitas = visitas,
            esCliente = usuario.esCliente
        )
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
