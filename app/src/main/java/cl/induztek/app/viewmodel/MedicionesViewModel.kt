package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.FormularioPrueba
import cl.induztek.app.model.MedicionesUiState
import cl.induztek.app.model.Momento
import cl.induztek.app.model.Prueba
import cl.induztek.app.model.PruebaRegistrada
import cl.induztek.app.model.ResultadoGuardado
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoPrueba
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
import java.time.LocalDate

class MedicionesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)

    private val _uiState = MutableStateFlow(MedicionesUiState())
    val uiState: StateFlow<MedicionesUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var idCargado: Long? = null

    fun cargar(idProtocolo: Long, usuarioActual: Usuario) {
        usuario = usuarioActual
        val protocolo = db.buscarProtocolo(idProtocolo)
        if (protocolo == null) {
            _uiState.value = MedicionesUiState(cargado = true)
            return
        }
        val equipo = db.buscarEquipo(protocolo.idEquipo, usuarioActual)
        val visita = db.buscarVisita(protocolo.idVisita)
        val estado = TipoEstadoProtocolo.desdeId(protocolo.idEstadoProtocolo)
        val tipos = equipo?.let { db.listarPruebasDeTipoEquipo(it.idTipoEquipo) } ?: emptyList()
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
                motivoUltimoCambio = motivoDelUltimoCambio(idProtocolo, estado),
                tiposPrueba = tipos,
                instrumentosVigentes = db.listarInstrumentosVigentes(LocalDate.now()),
                pruebas = armarPruebas(idProtocolo, tipos),
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
        val guardado = db.actualizarCondicionesAmbientales(
            protocolo.idProtocolo,
            Unidades.parsearDecimal(s.temperatura)!!,
            Unidades.parsearDecimal(s.humedad)!!
        )
        _uiState.update {
            if (guardado) it.copy(condicionesGuardadas = true, mensaje = "Condiciones ambientales guardadas", errorGeneral = null)
            else it.copy(errorGeneral = "No se pudieron guardar las condiciones ambientales.")
        }
    }

    fun onTipoPruebaSeleccionado(idTipoPrueba: Long) {
        val parametros = db.listarParametrosDeTipoPrueba(idTipoPrueba)
        val codigo = _uiState.value.tiposPrueba.firstOrNull { it.idTipoPrueba == idTipoPrueba }?.codigo
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

        val prueba = Prueba(
            idProtocolo = protocolo.idProtocolo,
            idTipoPrueba = tipo.idTipoPrueba,
            elemento = f.elemento.trim(),
            momento = f.momento,
            realizada = f.realizada,
            motivoNoRealizada = if (f.realizada) null else f.motivo.trim(),
            idInstrumento = if (f.realizada) f.idInstrumento else null,
            idResultado = resultado?.id,
            comentario = if (f.realizada && tipo.codigo == "OPERACION_RELE" && !f.opero) "El relé no operó" else null
        )

        when (db.insertarPrueba(prueba, mediciones)) {
            is ResultadoGuardado.Creado -> _uiState.update {
                it.copy(
                    pruebas = armarPruebas(protocolo.idProtocolo, it.tiposPrueba),

                    formulario = FormularioPrueba(
                        idTipoPrueba = f.idTipoPrueba, momento = f.momento, unidadAislacion = f.unidadAislacion,
                        unidadBaja = f.unidadBaja, idInstrumento = f.idInstrumento
                    ),
                    mensaje = "Prueba guardada",
                    errorGeneral = null
                )
            }
            is ResultadoGuardado.Duplicado -> _uiState.update {
                it.copy(formulario = f.copy(errores = mapOf("elemento" to "Ya registraste esta prueba para ese elemento y momento")))
            }
            ResultadoGuardado.Fallo -> _uiState.update {
                it.copy(errorGeneral = "No se pudo guardar la prueba. Intenta de nuevo.")
            }
        }
    }

    fun onEliminarPrueba(idPrueba: Long) {
        val s = _uiState.value
        val protocolo = s.protocolo ?: return
        if (!s.puedeModificar) return
        db.eliminarPrueba(idPrueba)
        _uiState.update {
            it.copy(pruebas = armarPruebas(protocolo.idProtocolo, it.tiposPrueba), mensaje = "Prueba eliminada")
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
        if (db.cambiarEstadoProtocolo(protocolo.idProtocolo, TipoEstadoProtocolo.REGISTRADO_TERRENO, u.idUsuario)) {
            cargar(protocolo.idProtocolo, u)
            _uiState.update { it.copy(mensaje = "Protocolo registrado en terreno", errorGeneral = null) }
        } else {
            _uiState.update { it.copy(errorGeneral = "No se pudo cambiar el estado del protocolo.") }
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
        if (db.cambiarEstadoProtocolo(protocolo.idProtocolo, nuevo, u.idUsuario, motivo)) {
            cargar(protocolo.idProtocolo, u)
            _uiState.update {
                it.copy(mensaje = mensajeExito, errorGeneral = null, motivoRevision = "", errorMotivoRevision = null)
            }
        } else {
            _uiState.update { it.copy(errorGeneral = "No se pudo cambiar el estado del protocolo.") }
        }
    }

    private fun motivoDelUltimoCambio(idProtocolo: Long, estado: TipoEstadoProtocolo): String? {
        if (estado != TipoEstadoProtocolo.OBSERVADO && estado != TipoEstadoProtocolo.ANULADO) return null
        return db.listarHistorialEstados(idProtocolo)
            .filter { it.idEstadoNuevo == estado.id }
            .maxByOrNull { it.fechaHora }
            ?.motivo
    }

    private fun armarPruebas(idProtocolo: Long, tipos: List<TipoPrueba>): List<PruebaRegistrada> =
        db.listarPruebas(idProtocolo).map { prueba ->
            val tipo = tipos.firstOrNull { it.idTipoPrueba == prueba.idTipoPrueba }
            val parametros = db.listarParametrosDeTipoPrueba(prueba.idTipoPrueba).associateBy { it.idParametro }
            PruebaRegistrada(
                idPrueba = prueba.idPrueba,
                nombreTipoPrueba = tipo?.nombre ?: "",
                elemento = prueba.elemento,
                momento = prueba.momento,
                realizada = prueba.realizada,
                motivoNoRealizada = prueba.motivoNoRealizada,
                resultado = prueba.idResultado?.let { TipoResultado.desdeId(it) },
                lineas = db.listarMediciones(prueba.idPrueba).mapNotNull { m ->
                    parametros[m.idParametro]?.let { p ->
                        val unidad = if (p.unidad == "—") "" else " ${p.unidad}"
                        "${p.nombre}: ${Unidades.formatear(m.valor)}$unidad"
                    }
                }
            )
        }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
