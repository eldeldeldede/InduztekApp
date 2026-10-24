package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.ActaProtocolo
import cl.induztek.app.model.CierreVisitaUiState
import cl.induztek.app.model.ProtocoloPendiente
import cl.induztek.app.model.RecepcionActa
import cl.induztek.app.model.ResumenProtocolo
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.model.Usuario
import cl.induztek.app.validacion.ReglasProtocolo
import cl.induztek.app.validacion.Unidades
import cl.induztek.app.validacion.ValidacionesFormulario
import cl.induztek.app.validacion.ValidadorRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class CierreVisitaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)

    private val _uiState = MutableStateFlow(CierreVisitaUiState())
    val uiState: StateFlow<CierreVisitaUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var idCargado: Long? = null

    fun cargar(idVisita: Long, usuarioActual: Usuario) {
        usuario = usuarioActual
        val visita = db.buscarVisita(idVisita)
        if (visita == null) {
            _uiState.value = CierreVisitaUiState(cargado = true)
            return
        }
        val protocolos = db.listarProtocolosPorVisita(idVisita, usuarioActual)
        val primeraVez = idCargado != idVisita
        idCargado = idVisita

        _uiState.update { previo ->
            val base = if (primeraVez) CierreVisitaUiState() else previo
            base.copy(
                cargado = true,
                visita = visita,
                instalacion = db.buscarInstalacion(visita.idInstalacion, usuarioActual),
                ingenieroNombre = db.buscarUsuarioPorId(visita.idUsuario)?.nombre ?: "",
                participantes = db.listarNombresParticipantes(idVisita),
                protocolos = protocolos,
                acta = armarActa(protocolos, usuarioActual),
                pendientes = armarPendientes(protocolos, usuarioActual),
                recepcion = db.buscarRecepcion(idVisita)
            )
        }
    }

    fun onNombreCambia(valor: String) = _uiState.update { it.copy(receptorNombre = valor, errorNombre = null, mensaje = null) }
    fun onRutCambia(valor: String) = _uiState.update { it.copy(receptorRut = valor, errorRut = null, mensaje = null) }
    fun onCargoCambia(valor: String) = _uiState.update { it.copy(receptorCargo = valor, errorCargo = null, mensaje = null) }

    fun onCerrarVisita() {
        val s = _uiState.value
        val visita = s.visita ?: return
        val u = usuario ?: return

        val error = ReglasProtocolo.errorCierreVisita(s.protocolos.map { it.estado })
        if (error != null) {
            val pendientes = s.pendientes.joinToString("; ") { p ->
                val detalle = if (p.pruebasFaltantes.isEmpty()) "falta registrarlo en terreno"
                else "faltan ${p.pruebasFaltantes.joinToString()}"
                "${p.numero} (${p.estado.etiqueta}): $detalle"
            }
            _uiState.update {
                it.copy(errorGeneral = if (pendientes.isEmpty()) error else "$error Faltan: $pendientes", mensaje = null)
            }
            return
        }
        if (db.cerrarVisita(visita.idVisita, LocalDateTime.now())) {
            cargar(visita.idVisita, u)
            _uiState.update { it.copy(mensaje = "Visita cerrada. Entrega el acta al cliente.", errorGeneral = null) }
        } else {
            _uiState.update { it.copy(errorGeneral = "No se pudo cerrar la visita (quizás ya estaba cerrada).") }
        }
    }

    fun onRegistrarRecepcion() {
        val s = _uiState.value
        val visita = s.visita ?: return
        val u = usuario ?: return
        if (!s.estaCerrada) {
            _uiState.update { it.copy(errorGeneral = "Primero debes cerrar la visita.") }
            return
        }

        val errorNombre = ValidacionesFormulario.nombreReceptor(s.receptorNombre)
        val errorRut = ValidadorRut.error(s.receptorRut)
        val errorCargo = ValidacionesFormulario.cargoReceptor(s.receptorCargo)
        if (errorNombre != null || errorRut != null || errorCargo != null) {
            _uiState.update { it.copy(errorNombre = errorNombre, errorRut = errorRut, errorCargo = errorCargo) }
            return
        }

        val guardado = db.insertarRecepcion(
            RecepcionActa(
                idVisita = visita.idVisita,
                receptorNombre = s.receptorNombre,
                receptorRut = ValidadorRut.formatear(s.receptorRut),
                receptorCargo = s.receptorCargo,
                fechaHoraRecepcion = LocalDateTime.now()
            )
        )
        if (guardado) {
            cargar(visita.idVisita, u)
            _uiState.update { it.copy(mensaje = "Recepción del acta registrada", errorGeneral = null) }
        } else {
            _uiState.update { it.copy(errorGeneral = "No se pudo registrar la recepción (quizás ya estaba registrada).") }
        }
    }

    private fun armarPendientes(protocolos: List<ResumenProtocolo>, usuarioActual: Usuario): List<ProtocoloPendiente> =
        protocolos
            .filter { it.estado != TipoEstadoProtocolo.REGISTRADO_TERRENO && it.estado != TipoEstadoProtocolo.ANULADO }
            .map { resumen ->
                val equipo = db.buscarEquipo(resumen.idEquipo, usuarioActual)
                val tipos = equipo?.let { db.listarPruebasDeTipoEquipo(it.idTipoEquipo) } ?: emptyList()
                val registradas = db.listarPruebas(resumen.idProtocolo).map { it.idTipoPrueba }.toSet()
                ProtocoloPendiente(
                    idProtocolo = resumen.idProtocolo,
                    numero = resumen.numero,
                    tagEquipo = resumen.tagEquipo,
                    estado = resumen.estado,
                    pruebasFaltantes = tipos.filter { it.idTipoPrueba !in registradas }.map { it.nombre }
                )
            }

    private fun armarActa(protocolos: List<ResumenProtocolo>, usuarioActual: Usuario): List<ActaProtocolo> =
        protocolos.filter { it.estado != TipoEstadoProtocolo.ANULADO }.map { resumen ->
            val equipo = db.buscarEquipo(resumen.idEquipo, usuarioActual)
            val tipos = equipo?.let { db.listarPruebasDeTipoEquipo(it.idTipoEquipo) } ?: emptyList()
            val lineas = mutableListOf<String>()
            db.listarPruebas(resumen.idProtocolo).forEach { prueba ->
                val nombreTipo = tipos.firstOrNull { it.idTipoPrueba == prueba.idTipoPrueba }?.nombre ?: ""
                val resultado = prueba.idResultado?.let { TipoResultado.desdeId(it).etiqueta } ?: "No realizada"
                lineas += "$nombreTipo · ${prueba.elemento} · $resultado"
                if (prueba.realizada) {
                    val parametros = db.listarParametrosDeTipoPrueba(prueba.idTipoPrueba).associateBy { it.idParametro }
                    db.listarMediciones(prueba.idPrueba).forEach { m ->
                        parametros[m.idParametro]?.let { p ->
                            val unidad = if (p.unidad == "—") "" else " ${p.unidad}"
                            lineas += "    ${p.nombre}: ${Unidades.formatear(m.valor)}$unidad"
                        }
                    }
                } else {
                    prueba.motivoNoRealizada?.let { lineas += "    Motivo: $it" }
                }
            }
            ActaProtocolo(resumen.numero, resumen.tagEquipo, resumen.resultadoGlobal, lineas)
        }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
