package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.network.ProtocoloDto
import cl.induztek.app.network.RecepcionSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.model.ActaProtocolo
import cl.induztek.app.model.CierreVisitaUiState
import cl.induztek.app.model.ProtocoloPendiente
import cl.induztek.app.model.ResumenProtocolo
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.model.Usuario
import cl.induztek.app.validacion.ReglasProtocolo
import cl.induztek.app.validacion.ValidacionesFormulario
import cl.induztek.app.validacion.ValidadorRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  CierreVisitaViewModel — acta de la visita, cierre y recepción por el cliente
//  Todo se consulta/guarda en protocolos-service a través del repositorio; las reglas de cierre se
//  validan aquí (para avisar al técnico) y de nuevo en el servidor (la regla definitiva).
// =====================================================================
class CierreVisitaViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CierreVisitaUiState())
    val uiState: StateFlow<CierreVisitaUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var idCargado: Long? = null

    fun cargar(idVisita: Long, usuarioActual: Usuario) {
        usuario = usuarioActual
        viewModelScope.launch { refrescar(idVisita, usuarioActual) }
    }

    private suspend fun refrescar(idVisita: Long, usuarioActual: Usuario) {
        val visitaRes = Repositorios.protocolos.visita(idVisita)
        val visita = (visitaRes as? ResultadoRed.Exito)?.datos
        if (visita == null) {
            _uiState.value = CierreVisitaUiState(cargado = true, errorGeneral = visitaRes.mensajeError())
            return
        }
        // Si la instalación no es de la empresa del cliente, el servidor responde error y no se muestra nada.
        val instalacion = (Repositorios.equipos.instalacion(visita.idInstalacion, usuarioActual) as? ResultadoRed.Exito)?.datos
        if (instalacion == null || (usuarioActual.esCliente && !visita.estaCerrada)) {
            _uiState.value = CierreVisitaUiState(cargado = true)
            return
        }
        val protocolos = (Repositorios.protocolos.protocolosPorVisita(idVisita, visita.idInstalacion, usuarioActual)
            as? ResultadoRed.Exito)?.datos ?: emptyList()
        val nombres = Repositorios.usuarios.nombres()
        val idsParticipantes = (Repositorios.protocolos.participantes(idVisita) as? ResultadoRed.Exito)?.datos ?: emptyList()
        val recepcion = (Repositorios.protocolos.recepcion(idVisita) as? ResultadoRed.Exito)?.datos
        val (acta, pendientes) = armarActaYPendientes(protocolos, usuarioActual)

        val primeraVez = idCargado != idVisita
        idCargado = idVisita
        _uiState.update { previo ->
            val base = if (primeraVez) CierreVisitaUiState() else previo
            base.copy(
                cargado = true,
                visita = visita,
                instalacion = instalacion,
                soloConsulta = usuarioActual.esCliente,
                puedeFormalizar = usuarioActual.codigoRol == CodigoRol.INGENIERO || usuarioActual.codigoRol == CodigoRol.JEFE_TECNICO,
                ingenieroNombre = nombres[visita.idUsuario] ?: "",
                participantes = idsParticipantes.mapNotNull { nombres[it] },
                protocolos = protocolos,
                acta = acta,
                pendientes = pendientes,
                recepcion = recepcion
            )
        }
    }

    fun onNombreCambia(valor: String) = _uiState.update { it.copy(receptorNombre = valor, errorNombre = null, mensaje = null) }
    fun onRutCambia(valor: String) = _uiState.update { it.copy(receptorRut = valor, errorRut = null, mensaje = null) }

    fun onRutTermina() = _uiState.update { it.copy(receptorRut = ValidadorRut.formatearEntrada(it.receptorRut)) }
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
        viewModelScope.launch {
            when (val r = Repositorios.protocolos.cerrarVisita(visita.idVisita, u.idUsuario)) {
                is ResultadoRed.Exito -> {
                    refrescar(visita.idVisita, u)
                    _uiState.update { it.copy(mensaje = "Visita cerrada. Entrega el acta al cliente.", errorGeneral = null) }
                }
                else -> _uiState.update { it.copy(errorGeneral = r.mensajeError(), mensaje = null) }
            }
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

        viewModelScope.launch {
            val solicitud = RecepcionSolicitud(
                receptorNombre = s.receptorNombre.trim(),
                receptorRut = ValidadorRut.formatear(s.receptorRut),
                receptorCargo = s.receptorCargo.trim()
            )
            when (val r = Repositorios.protocolos.registrarRecepcion(visita.idVisita, solicitud)) {
                is ResultadoRed.Exito -> {
                    refrescar(visita.idVisita, u)
                    _uiState.update { it.copy(mensaje = "Recepción del acta registrada", errorGeneral = null) }
                }
                else -> _uiState.update { it.copy(errorGeneral = r.mensajeError(), mensaje = null) }
            }
        }
    }

    // Arma, con el detalle de cada protocolo (pruebas y mediciones), el texto del acta y la lista de pendientes.
    private suspend fun armarActaYPendientes(
        protocolos: List<ResumenProtocolo>,
        usuarioActual: Usuario
    ): Pair<List<ActaProtocolo>, List<ProtocoloPendiente>> {
        val acta = mutableListOf<ActaProtocolo>()
        val pendientes = mutableListOf<ProtocoloPendiente>()
        val tiposPorEquipo = mutableMapOf<Long, List<cl.induztek.app.model.TipoPrueba>>()

        for (resumen in protocolos.filter { it.estado != TipoEstadoProtocolo.ANULADO }) {
            val detalle: ProtocoloDto = (Repositorios.protocolos.protocoloDetalle(resumen.idProtocolo)
                as? ResultadoRed.Exito)?.datos ?: continue
            val pruebas = detalle.pruebas ?: emptyList()

            val lineas = mutableListOf<String>()
            pruebas.forEach { prueba ->
                val resultado = prueba.resultado?.let { TipoResultado.valueOf(it).etiqueta } ?: "No realizada"
                lineas += "${prueba.tipoPruebaNombre ?: prueba.tipoPruebaCodigo} · ${prueba.elemento} · $resultado"
                if (prueba.realizada) {
                    (prueba.mediciones ?: emptyList()).forEach { m ->
                        val unidad = if (m.unidad.isNullOrBlank() || m.unidad == "—") "" else " ${m.unidad}"
                        lineas += "    ${m.parametroNombre ?: m.parametroCodigo}: ${cl.induztek.app.validacion.Unidades.formatear(m.valor)}$unidad"
                    }
                } else {
                    prueba.motivoNoRealizada?.let { lineas += "    Motivo: $it" }
                }
            }
            acta += ActaProtocolo(resumen.idProtocolo, resumen.numero, resumen.tagEquipo, resumen.resultadoGlobal, lineas, resumen.estado)

            if (resumen.estado != TipoEstadoProtocolo.REGISTRADO_TERRENO) {
                val equipo = (Repositorios.equipos.equipo(resumen.idEquipo, usuarioActual) as? ResultadoRed.Exito)?.datos
                val tipos = if (equipo == null) emptyList() else tiposPorEquipo.getOrPut(equipo.idTipoEquipo) {
                    (Repositorios.protocolos.pruebasDeTipoEquipo(equipo.idTipoEquipo) as? ResultadoRed.Exito)?.datos ?: emptyList()
                }
                val registradas = pruebas.map { it.tipoPruebaId }.toSet()
                pendientes += ProtocoloPendiente(
                    idProtocolo = resumen.idProtocolo,
                    numero = resumen.numero,
                    tagEquipo = resumen.tagEquipo,
                    estado = resumen.estado,
                    pruebasFaltantes = tipos.filter { it.idTipoPrueba !in registradas }.map { it.nombre }
                )
            }
        }
        return acta to pendientes
    }
}
