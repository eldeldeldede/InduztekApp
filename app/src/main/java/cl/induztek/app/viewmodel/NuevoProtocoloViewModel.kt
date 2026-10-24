package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.NuevoProtocoloUiState
import cl.induztek.app.model.ResultadoGuardado
import cl.induztek.app.model.TipoVisita
import cl.induztek.app.model.Usuario
import cl.induztek.app.model.Visita
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// =====================================================================
//  NuevoProtocoloViewModel.kt — HU-02: iniciar un protocolo para un equipo.
//  Pasos al guardar:  1) crear la visita (o reutilizar la de esa OT)  2) crear el protocolo
//  en estado Borrador.  Después la pantalla navega a "Mediciones".
//  Las reglas viven en la base (OT única, un protocolo por equipo y visita) y se traducen
//  aquí en mensajes para el usuario, igual que en AltaEquipoViewModel.
// =====================================================================
class NuevoProtocoloViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)

    // Los ingenieros no cambian mientras la app corre: se leen una sola vez
    private val _uiState = MutableStateFlow(
        NuevoProtocoloUiState(ingenieros = db.listarUsuariosActivosPorRol(CodigoRol.INGENIERO))
    )
    val uiState: StateFlow<NuevoProtocoloUiState> = _uiState.asStateFlow()

    fun cargar(idEquipo: Long, usuario: Usuario) {
        _uiState.update { it.copy(equipo = db.buscarEquipo(idEquipo, usuario)) }
    }

    fun onOtCambia(valor: String) = _uiState.update {
        it.copy(numeroOt = valor.uppercase(), errorOt = null, errorGeneral = null)
    }
    fun onTipoVisitaCambia(tipo: TipoVisita) = _uiState.update { it.copy(tipoVisita = tipo) }
    fun onIngenieroSeleccionado(id: Long) = _uiState.update { it.copy(idIngeniero = id, errorIngeniero = null) }

    fun onGuardar(usuario: Usuario) {
        val e = _uiState.value
        val equipo = e.equipo ?: return

        val errorOt = ValidacionesFormulario.numeroOt(e.numeroOt)
        val errorIngeniero = if (e.idIngeniero == null) "Elige el ingeniero a cargo de la visita" else null
        if (errorOt != null || errorIngeniero != null) {
            _uiState.update { it.copy(errorOt = errorOt, errorIngeniero = errorIngeniero) }
            return
        }

        // 1) La visita. Aclaración 7: toda visita tiene un ingeniero a cargo, aunque no asista.
        val visita = Visita(
            numeroOt = e.numeroOt.trim(),
            idInstalacion = equipo.idInstalacion,
            fecha = LocalDate.now(),
            tipoVisita = e.tipoVisita,
            idUsuario = e.idIngeniero!!
        )
        val idVisita = when (val r = db.insertarVisita(visita, listOf(usuario.idUsuario))) {
            is ResultadoGuardado.Creado -> r.id
            is ResultadoGuardado.Duplicado -> {
                // La OT ya existía: se reutiliza SOLO si es de esta instalación y sigue abierta
                val existente = db.buscarVisita(r.idExistente)
                when {
                    existente == null || existente.idInstalacion != equipo.idInstalacion -> {
                        _uiState.update { it.copy(errorOt = "Esa OT pertenece a otra instalación") }
                        return
                    }
                    existente.estaCerrada -> {
                        _uiState.update { it.copy(errorOt = "Esa visita ya está cerrada") }
                        return
                    }
                    else -> existente.idVisita
                }
            }
            ResultadoGuardado.Fallo -> {
                _uiState.update { it.copy(errorGeneral = "No se pudo guardar la visita. Intenta de nuevo.") }
                return
            }
        }

        // 2) El protocolo en Borrador. Si el equipo ya tenía uno en esa visita, se continúa ese.
        when (val r = db.insertarProtocoloBorrador(idVisita, equipo.idEquipo, usuario.idUsuario)) {
            is ResultadoGuardado.Creado -> _uiState.update { it.copy(idProtocoloCreado = r.id) }
            is ResultadoGuardado.Duplicado -> _uiState.update { it.copy(idProtocoloCreado = r.idExistente) }
            ResultadoGuardado.Fallo ->
                _uiState.update { it.copy(errorGeneral = "No se pudo crear el protocolo. Intenta de nuevo.") }
        }
    }

    // La pantalla ya navegó: se limpia para que, al volver, no navegue otra vez
    fun onNavegado() = _uiState.update { it.copy(idProtocoloCreado = null) }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
