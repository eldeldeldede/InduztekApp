package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.NuevoProtocoloUiState
import cl.induztek.app.model.TipoVisita
import cl.induztek.app.model.Usuario
import cl.induztek.app.model.Visita
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

// =====================================================================
//  NuevoProtocoloViewModel — crea (o reutiliza) la visita por su OT y luego el protocolo borrador del equipo
// =====================================================================
class NuevoProtocoloViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NuevoProtocoloUiState())
    val uiState: StateFlow<NuevoProtocoloUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val r = Repositorios.usuarios.listarActivosPorRol(CodigoRol.INGENIERO)
            if (r is ResultadoRed.Exito) _uiState.update { it.copy(ingenieros = r.datos) }
            else _uiState.update { it.copy(errorGeneral = r.mensajeError()) }
            val t = Repositorios.usuarios.listarActivosPorRol(CodigoRol.TECNICO)
            if (t is ResultadoRed.Exito) _uiState.update { it.copy(tecnicos = t.datos) }
        }
    }

    fun cargar(idEquipo: Long, usuario: Usuario) {
        _uiState.update { it.copy(idActor = usuario.idUsuario) }
        viewModelScope.launch {
            val r = Repositorios.equipos.equipo(idEquipo, usuario)
            _uiState.update { it.copy(equipo = (r as? ResultadoRed.Exito)?.datos, errorGeneral = r.mensajeError()) }
        }
    }

    fun onOtCambia(valor: String) = _uiState.update {
        it.copy(numeroOt = valor.uppercase(), errorOt = null, errorGeneral = null)
    }
    fun onTipoVisitaCambia(tipo: TipoVisita) = _uiState.update { it.copy(tipoVisita = tipo) }
    fun onParticipanteAlternado(id: Long) = _uiState.update {
        val nuevos = if (id in it.idsOtrosParticipantes) it.idsOtrosParticipantes - id else it.idsOtrosParticipantes + id
        it.copy(idsOtrosParticipantes = nuevos)
    }
    fun onIngenieroSeleccionado(id: Long) = _uiState.update {
        it.copy(idIngeniero = id, errorIngeniero = null, idsOtrosParticipantes = it.idsOtrosParticipantes - id)
    }

    fun onGuardar(usuario: Usuario) {
        val e = _uiState.value
        val equipo = e.equipo ?: return

        val errorOt = ValidacionesFormulario.numeroOt(e.numeroOt)
        val errorIngeniero = if (e.idIngeniero == null) "Elige el ingeniero a cargo de la visita" else null
        if (errorOt != null || errorIngeniero != null) {
            _uiState.update { it.copy(errorOt = errorOt, errorIngeniero = errorIngeniero) }
            return
        }

        val visita = Visita(
            numeroOt = e.numeroOt.trim(),
            idInstalacion = equipo.idInstalacion,
            fecha = LocalDate.now(),
            tipoVisita = e.tipoVisita,
            idUsuario = e.idIngeniero!!
        )
        viewModelScope.launch {
            val participantes = (listOf(usuario.idUsuario) + e.idsOtrosParticipantes).distinct()
            val idVisita = obtenerIdVisita(visita, participantes, usuario) ?: return@launch
            when (val r = Repositorios.protocolos.crearProtocolo(idVisita, equipo.idEquipo, usuario.idUsuario)) {
                is ResultadoRed.Exito -> _uiState.update { it.copy(idProtocoloCreado = r.datos.idProtocolo) }
                is ResultadoRed.ErrorServidor ->
                    if (r.codigo == 409) {
                        // Ya existía un protocolo de este equipo en la visita: se abre ese en vez de duplicarlo.
                        val existente = Repositorios.protocolos.idProtocoloExistente(idVisita, equipo.idEquipo)
                        if (existente != null) _uiState.update { it.copy(idProtocoloCreado = existente) }
                        else _uiState.update { it.copy(errorGeneral = r.mensaje) }
                    } else _uiState.update { it.copy(errorGeneral = r.mensaje) }
                ResultadoRed.SinConexion -> _uiState.update { it.copy(errorGeneral = r.mensajeError()) }
            }
        }
    }

    // Crea la visita; si la OT ya existe (409) reutiliza la existente siempre que sea de la misma instalación y siga abierta.
    private suspend fun obtenerIdVisita(visita: Visita, participantes: List<Long>, usuario: Usuario): Long? {
        return when (val r = Repositorios.protocolos.crearVisita(visita, participantes, usuario.idUsuario)) {
            is ResultadoRed.Exito -> r.datos.idVisita
            is ResultadoRed.ErrorServidor -> {
                if (r.codigo != 409) {
                    _uiState.update { it.copy(errorGeneral = r.mensaje) }
                    return null
                }
                val existente = (Repositorios.protocolos.visitaPorOt(visita.numeroOt) as? ResultadoRed.Exito)?.datos
                when {
                    existente == null || existente.idInstalacion != visita.idInstalacion -> {
                        _uiState.update { it.copy(errorOt = "Esa OT pertenece a otra instalación") }
                        null
                    }
                    existente.estaCerrada -> {
                        _uiState.update { it.copy(errorOt = "Esa visita ya está cerrada") }
                        null
                    }
                    else -> existente.idVisita
                }
            }
            ResultadoRed.SinConexion -> {
                _uiState.update { it.copy(errorGeneral = r.mensajeError()) }
                null
            }
        }
    }

    fun onNavegado() = _uiState.update { it.copy(idProtocoloCreado = null) }
}
