package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.local.ContextoPruebaLocal
import cl.induztek.app.model.FormularioPrueba
import cl.induztek.app.model.MedicionesUiState
import cl.induztek.app.model.Momento
import cl.induztek.app.model.PruebaRegistrada
import cl.induztek.app.network.MedicionSolicitud
import cl.induztek.app.network.ProtocoloDto
import cl.induztek.app.repository.aPruebaRegistrada
import cl.induztek.app.repository.aProtocolo
import cl.induztek.app.network.PruebaSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.model.TipoVisita
import cl.induztek.app.model.Usuario
import cl.induztek.app.validacion.EvaluadorPrueba
import cl.induztek.app.validacion.ReglasProtocolo
import cl.induztek.app.validacion.UnidadAislacion
import cl.induztek.app.validacion.UnidadBaja
import cl.induztek.app.validacion.Unidades
import cl.induztek.app.validacion.PruebaElectrica
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedicionesViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MedicionesUiState())
    val uiState: StateFlow<MedicionesUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var idCargado: Long? = null

    fun cargar(idProtocolo: Long, usuarioActual: Usuario) {
        usuario = usuarioActual
        viewModelScope.launch { refrescar(idProtocolo, usuarioActual) }
    }

    // Vuelve a pedir al servidor el protocolo con sus pruebas y deja la pantalla al día.
    private suspend fun refrescar(idProtocolo: Long, usuarioActual: Usuario) {
        val detalleRes = Repositorios.protocolos.protocoloDetalle(idProtocolo)
        val detalle = (detalleRes as? ResultadoRed.Exito)?.datos
        if (detalle == null) {
            _uiState.value = MedicionesUiState(cargado = true, errorGeneral = detalleRes.mensajeError())
            return
        }
        val protocolo = detalle.aProtocolo()
        val equipo = (Repositorios.equipos.equipo(protocolo.idEquipo, usuarioActual) as? ResultadoRed.Exito)?.datos
        val visita = (Repositorios.protocolos.visita(protocolo.idVisita) as? ResultadoRed.Exito)?.datos
        val estado = TipoEstadoProtocolo.desdeId(protocolo.idEstadoProtocolo)
        val tipos = equipo?.let {
            (Repositorios.protocolos.pruebasDeTipoEquipo(it.idTipoEquipo) as? ResultadoRed.Exito)?.datos
        } ?: emptyList()
        val instrumentos = (Repositorios.protocolos.instrumentosVigentes() as? ResultadoRed.Exito)?.datos ?: emptyList()
        val posibles = ReglasProtocolo.estadosPosibles(estado, usuarioActual.codigoRol)
        val primeraVez = idCargado != idProtocolo
        idCargado = idProtocolo

        _uiState.update { previo ->
            val base = if (primeraVez) MedicionesUiState() else previo
            base.copy(
                cargado = true,
                protocolo = protocolo,
                equipo = equipo,
                visita = visita,
                estado = estado,
                puedeModificar = ReglasProtocolo.puedeModificar(
                    usuarioActual.codigoRol, usuarioActual.idUsuario, protocolo.idUsuario, estado
                ),
                puedeRevisar = TipoEstadoProtocolo.FORMALIZADO in posibles,
                puedeAnular = TipoEstadoProtocolo.ANULADO in posibles,
                motivoUltimoCambio = motivoDelUltimoCambio(detalle, estado),
                tiposPrueba = tipos,
                instrumentosVigentes = instrumentos,
                pruebas = (detalle.pruebas ?: emptyList()).map { it.aPruebaRegistrada() },
                condicionesGuardadas = protocolo.temperaturaAmbiente != null && protocolo.pctHumedad != null,
                temperatura = if (primeraVez) protocolo.temperaturaAmbiente?.let { Unidades.formatear(it) } ?: ""
                else previo.temperatura,
                humedad = if (primeraVez) protocolo.pctHumedad?.let { Unidades.formatear(it) } ?: ""
                else previo.humedad,
                formulario = if (primeraVez) FormularioPrueba(
                    momento = if (visita?.tipoVisita == TipoVisita.PRUEBAS_Y_MANT) Momento.PREVIA else Momento.UNICA
                ) else previo.formulario
            )
        }
    }

    fun onTemperaturaCambia(valor: String) =
        _uiState.update { it.copy(temperatura = valor, errorTemperatura = null, mensaje = null) }
    fun onHumedadCambia(valor: String) =
        _uiState.update { it.copy(humedad = valor, errorHumedad = null, mensaje = null) }

    fun onGuardarCondiciones() {
        val s = _uiState.value
        val protocolo = s.protocolo ?: return
        val errorT = ValidacionesFormulario.temperatura(s.temperatura)
        val errorH = ValidacionesFormulario.humedad(s.humedad)
        if (errorT != null || errorH != null) {
            _uiState.update { it.copy(errorTemperatura = errorT, errorHumedad = errorH) }
            return
        }
        val u = usuario ?: return
        viewModelScope.launch {
            val r = Repositorios.protocolos.actualizarCondiciones(
                protocolo,
                Unidades.parsearDecimal(s.temperatura)!!,
                Unidades.parsearDecimal(s.humedad)!!,
                u.idUsuario
            )
            _uiState.update {
                if (r is ResultadoRed.Exito)
                    it.copy(protocolo = r.datos, condicionesGuardadas = true, mensaje = "Condiciones ambientales guardadas", errorGeneral = null)
                else it.copy(errorGeneral = r.mensajeError(), mensaje = null)
            }
        }
    }

    fun onTipoPruebaSeleccionado(idTipoPrueba: Long) {
        val idTipoEquipo = _uiState.value.equipo?.idTipoEquipo ?: return
        val codigo = _uiState.value.tiposPrueba.firstOrNull { it.idTipoPrueba == idTipoPrueba }?.codigo
        viewModelScope.launch {
            val parametros = (Repositorios.protocolos.parametrosDeTipoPrueba(idTipoEquipo, idTipoPrueba)
                as? ResultadoRed.Exito)?.datos ?: emptyList()
            _uiState.update { s ->
                val nuevo = FormularioPrueba(
                    idTipoPrueba = idTipoPrueba,
                    elemento = s.formulario.elemento,
                    momento = s.formulario.momento,
                    unidadBaja = if (codigo == "DEVANADOS") UnidadBaja.MOHM else UnidadBaja.UOHM
                )
                s.copy(parametros = parametros, formulario = nuevo, mensaje = null, errorGeneral = null)
            }
        }
    }

    fun onElementoCambia(valor: String) = cambiarFormulario {
        it.copy(elemento = valor.uppercase(), errores = it.errores - "elemento")
    }
    fun onMomentoCambia(momento: Momento) = cambiarFormulario { it.copy(momento = momento) }
    fun onRealizadaCambia(realizada: Boolean) = cambiarFormulario {
        it.copy(realizada = realizada, errores = emptyMap())
    }
    fun onMotivoCambia(valor: String) = cambiarFormulario { it.copy(motivo = valor, errores = it.errores - "motivo") }
    fun onValorCambia(codigoParametro: String, texto: String) = cambiarFormulario {
        it.copy(valores = it.valores + (codigoParametro to texto), errores = it.errores - codigoParametro)
    }
    fun onUnidadAislacionCambia(unidad: UnidadAislacion) = cambiarFormulario { it.copy(unidadAislacion = unidad) }
    fun onUnidadBajaCambia(unidad: UnidadBaja) = cambiarFormulario { it.copy(unidadBaja = unidad) }
    fun onOperoCambia(opero: Boolean) = cambiarFormulario { it.copy(opero = opero, errores = emptyMap()) }
    fun onInstrumentoSeleccionado(id: Long) = cambiarFormulario {
        it.copy(idInstrumento = id, errores = it.errores - "instrumento")
    }
    fun onResultadoManualSeleccionado(resultado: TipoResultado) = cambiarFormulario {
        it.copy(resultadoManual = resultado, errores = it.errores - "resultado")
    }

    private fun cambiarFormulario(cambio: (FormularioPrueba) -> FormularioPrueba) = _uiState.update { s ->
        val nuevo = cambio(s.formulario)
        s.copy(
            formulario = nuevo.copy(vistaPrevia = calcularVistaPrevia(s, nuevo)),
            mensaje = null,
            errorGeneral = null
        )
    }

    private fun calcularVistaPrevia(s: MedicionesUiState, f: FormularioPrueba): EvaluadorPrueba.Evaluacion? {
        val tipo = s.tipoPruebaElegido ?: return null
        if (!f.realizada) return null
        val evaluacion = EvaluadorPrueba.evaluar(
            codigoTipoPrueba = tipo.codigo,
            valores = valoresEnUnidadBase(s, f),
            maxContactosUohm = s.equipo?.maxContactosUohm,
            soloUnMinuto = s.soloUnMinuto,
            opero = f.opero
        )
        return if (evaluacion.resultado == null && evaluacion.detalle == null) null else evaluacion
    }

    private fun valoresEnUnidadBase(s: MedicionesUiState, f: FormularioPrueba): Map<String, Double> {
        val resultado = mutableMapOf<String, Double>()
        s.parametrosVisibles.forEach { p ->
            val valor = Unidades.parsearDecimal(f.valores[p.codigo].orEmpty()) ?: return@forEach
            resultado[p.codigo] = when (p.codigo) {
                "LECTURA_1MIN", "LECTURA_10MIN" -> Unidades.aMegaOhm(valor, f.unidadAislacion)
                "RESISTENCIA" -> Unidades.aMiliOhm(valor, f.unidadBaja)
                "RESISTENCIA_CONTACTO" -> Unidades.aMicroOhm(valor, f.unidadBaja)
                else -> valor
            }
        }
        return resultado
    }

    fun onGuardarPrueba() {
        val s = _uiState.value
        val protocolo = s.protocolo ?: return
        val tipo = s.tipoPruebaElegido
        val f = s.formulario

        if (!s.puedeModificar) {
            _uiState.update { it.copy(errorGeneral = "No tienes permiso para modificar este protocolo.") }
            return
        }
        if (tipo == null) {
            _uiState.update { it.copy(errorGeneral = "Elige el tipo de prueba.") }
            return
        }

        val errores = mutableMapOf<String, String>()
        if (f.elemento.isBlank()) errores["elemento"] = "Indica el elemento medido (ej.: AT-T, Fase H1-X1)"
        if (!f.realizada) {

            ValidacionesFormulario.motivo(f.motivo)?.let { errores["motivo"] = it }
        } else {
            if (s.instrumentosParaPrueba.isEmpty()) {
                errores["instrumento"] = "No hay instrumentos vigentes para esta prueba: márcala como no realizada"
            } else if (s.instrumentosParaPrueba.none { it.idInstrumento == f.idInstrumento }) {
                errores["instrumento"] = "Elige el instrumento (calibración vigente, RN-11)"
            }
            s.parametrosVisibles.forEach { p ->
                val texto = f.valores[p.codigo].orEmpty()
                val error = PruebaElectrica.desdeCodigo(tipo.codigo)?.errorParametro(p.codigo, p.nombre, texto)
                    ?: ValidacionesFormulario.medicion(texto, p.nombre)
                error?.let { errores[p.codigo] = it }
            }
            if (s.requiereResultadoManual && f.resultadoManual == null) errores["resultado"] = "Elige el resultado de la prueba"
        }
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(formulario = f.copy(errores = errores)) }
            return
        }

        val valoresBase = valoresEnUnidadBase(s, f)
        val resultado: TipoResultado? = if (!f.realizada) null
        else if (s.requiereResultadoManual) f.resultadoManual
        else EvaluadorPrueba.evaluar(
            tipo.codigo, valoresBase, s.equipo?.maxContactosUohm, s.soloUnMinuto, f.opero
        ).resultado
        if (f.realizada && resultado == null) {

            _uiState.update {
                it.copy(formulario = f.copy(errores = mapOf("resultado" to "No se pudo calcular el resultado: revisa los valores")))
            }
            return
        }

        val mediciones: Map<Long, Double> = if (!f.realizada) emptyMap()
        else s.parametrosVisibles.mapNotNull { p -> valoresBase[p.codigo]?.let { p.idParametro to it } }.toMap()

        val u = usuario ?: return
        val solicitud = PruebaSolicitud(
            tipoPruebaId = tipo.idTipoPrueba,
            elemento = f.elemento.trim(),
            momento = f.momento.name,
            realizada = f.realizada,
            motivoNoRealizada = if (f.realizada) null else f.motivo.trim(),
            instrumentoId = if (f.realizada) f.idInstrumento else null,
            noOpera = if (f.realizada && tipo.codigo == "OPERACION_RELE") !f.opero else null,
            resultadoManual = if (f.realizada && s.requiereResultadoManual) resultado?.name else null,
            comentario = if (f.realizada && tipo.codigo == "OPERACION_RELE" && !f.opero) "El relé no operó" else null,
            mediciones = if (f.realizada)
                s.parametrosVisibles.mapNotNull { p -> valoresBase[p.codigo]?.let { MedicionSolicitud(p.codigo, it) } }
            else emptyList()
        )
        viewModelScope.launch {
            when (val r = Repositorios.protocolos.registrarPrueba(
                protocolo.idProtocolo, solicitud, u.idUsuario,
                ContextoPruebaLocal(tipo, s.parametros, resultado?.name)
            )) {
                is ResultadoRed.Exito -> {
                    val pruebas = (Repositorios.protocolos.pruebas(protocolo.idProtocolo) as? ResultadoRed.Exito)?.datos
                    _uiState.update {
                        it.copy(
                            pruebas = pruebas ?: (it.pruebas + r.datos),
                            formulario = FormularioPrueba(
                                idTipoPrueba = f.idTipoPrueba, momento = f.momento, unidadAislacion = f.unidadAislacion,
                                unidadBaja = f.unidadBaja, idInstrumento = f.idInstrumento
                            ),
                            mensaje = "Prueba guardada",
                            errorGeneral = null
                        )
                    }
                }
                is ResultadoRed.ErrorServidor -> _uiState.update {
                    if (r.codigo == 409)
                        it.copy(formulario = f.copy(errores = mapOf("elemento" to "Ya registraste esta prueba para ese elemento y momento")))
                    else it.copy(errorGeneral = r.mensaje)
                }
                ResultadoRed.SinConexion -> _uiState.update { it.copy(errorGeneral = r.mensajeError()) }
            }
        }
    }

    fun onEliminarPrueba(idPrueba: Long) {
        val s = _uiState.value
        val protocolo = s.protocolo ?: return
        if (!s.puedeModificar) return
        val u = usuario ?: return
        viewModelScope.launch {
            when (val r = Repositorios.protocolos.eliminarPrueba(protocolo.idProtocolo, idPrueba, u.idUsuario)) {
                is ResultadoRed.Exito -> _uiState.update {
                    it.copy(pruebas = it.pruebas.filter { p -> p.idPrueba != idPrueba }, mensaje = "Prueba eliminada")
                }
                else -> _uiState.update { it.copy(errorGeneral = r.mensajeError(), mensaje = null) }
            }
        }
    }

    fun onRegistrarEnTerreno() {
        val s = _uiState.value
        val protocolo = s.protocolo ?: return
        val u = usuario ?: return

        val error = ReglasProtocolo.errorTransicion(
            desde = s.estado,
            hacia = TipoEstadoProtocolo.REGISTRADO_TERRENO,
            rol = u.codigoRol,
            tienePruebaCompleta = s.pruebas.any { it.realizada },
            tieneCondicionesAmbientales = s.condicionesGuardadas
        ) ?: if (s.tiposSinRegistro.isNotEmpty())
            "Faltan pruebas: ${s.tiposSinRegistro.joinToString { it.nombre }}. " +
                "Regístralas o márcalas como no realizadas con su motivo."
        else null

        if (error != null) {
            _uiState.update { it.copy(errorGeneral = error, mensaje = null) }
            return
        }
        viewModelScope.launch {
            val r = Repositorios.protocolos.cambiarEstado(protocolo.idProtocolo, TipoEstadoProtocolo.REGISTRADO_TERRENO, u.idUsuario)
            if (r is ResultadoRed.Exito) {
                refrescar(protocolo.idProtocolo, u)
                _uiState.update { it.copy(mensaje = "Protocolo registrado en terreno", errorGeneral = null) }
            } else _uiState.update { it.copy(errorGeneral = r.mensajeError(), mensaje = null) }
        }
    }

    fun onMotivoRevisionCambia(valor: String) =
        _uiState.update { it.copy(motivoRevision = valor, errorMotivoRevision = null) }

    fun onFormalizar() = cambiarEstadoRevision(TipoEstadoProtocolo.FORMALIZADO, "Protocolo formalizado")

    fun onObservar() = cambiarEstadoRevision(TipoEstadoProtocolo.OBSERVADO, "Protocolo devuelto con observaciones")

    fun onAnular() = cambiarEstadoRevision(TipoEstadoProtocolo.ANULADO, "Protocolo anulado")

    private fun cambiarEstadoRevision(nuevo: TipoEstadoProtocolo, mensajeExito: String) {
        val s = _uiState.value
        val protocolo = s.protocolo ?: return
        val u = usuario ?: return

        val pideMotivo = nuevo != TipoEstadoProtocolo.FORMALIZADO
        if (pideMotivo) {
            val errorMotivo = ValidacionesFormulario.motivo(s.motivoRevision)
            if (errorMotivo != null) {
                _uiState.update { it.copy(errorMotivoRevision = errorMotivo, errorGeneral = null, mensaje = null) }
                return
            }
        }
        val motivo = if (pideMotivo) s.motivoRevision.trim() else null

        val error = ReglasProtocolo.errorTransicion(s.estado, nuevo, u.codigoRol, motivo)
        if (error != null) {
            _uiState.update { it.copy(errorGeneral = error, mensaje = null) }
            return
        }
        viewModelScope.launch {
            val r = Repositorios.protocolos.cambiarEstado(protocolo.idProtocolo, nuevo, u.idUsuario, motivo)
            if (r is ResultadoRed.Exito) {
                refrescar(protocolo.idProtocolo, u)
                _uiState.update {
                    it.copy(mensaje = mensajeExito, errorGeneral = null, motivoRevision = "", errorMotivoRevision = null)
                }
            } else _uiState.update { it.copy(errorGeneral = r.mensajeError(), mensaje = null) }
        }
    }

    // Motivo escrito en el último cambio al estado actual (solo para OBSERVADO y ANULADO).
    private fun motivoDelUltimoCambio(detalle: ProtocoloDto, estado: TipoEstadoProtocolo): String? {
        if (estado != TipoEstadoProtocolo.OBSERVADO && estado != TipoEstadoProtocolo.ANULADO) return null
        return (detalle.historial ?: emptyList())
            .filter { it.estadoNuevo == estado.name }
            .maxByOrNull { it.fechaHora }
            ?.motivo
    }
}
