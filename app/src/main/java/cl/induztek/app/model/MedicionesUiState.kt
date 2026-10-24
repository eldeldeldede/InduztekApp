package cl.induztek.app.model

import cl.induztek.app.validacion.EvaluadorPrueba
import cl.induztek.app.validacion.InstrumentosPorPrueba
import cl.induztek.app.validacion.UnidadAislacion
import cl.induztek.app.validacion.UnidadBaja

data class PruebaRegistrada(
    val idPrueba: Long,
    val nombreTipoPrueba: String,
    val elemento: String,
    val momento: Momento,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val resultado: TipoResultado?,
    val lineas: List<String>
)

data class FormularioPrueba(
    val idTipoPrueba: Long? = null,
    val elemento: String = "",
    val momento: Momento = Momento.UNICA,
    val realizada: Boolean = true,
    val motivo: String = "",
    val valores: Map<String, String> = emptyMap(),
    val unidadAislacion: UnidadAislacion = UnidadAislacion.MOHM,
    val unidadBaja: UnidadBaja = UnidadBaja.UOHM,
    val opero: Boolean = true,
    val idInstrumento: Long? = null,
    val resultadoManual: TipoResultado? = null,

    val errores: Map<String, String> = emptyMap(),

    val vistaPrevia: EvaluadorPrueba.Evaluacion? = null
)

data class MedicionesUiState(
    val cargado: Boolean = false,
    val protocolo: Protocolo? = null,
    val equipo: Equipo? = null,
    val visita: Visita? = null,
    val estado: TipoEstadoProtocolo = TipoEstadoProtocolo.BORRADOR,
    val puedeModificar: Boolean = false,

    val temperatura: String = "",
    val humedad: String = "",
    val errorTemperatura: String? = null,
    val errorHumedad: String? = null,
    val condicionesGuardadas: Boolean = false,

    val tiposPrueba: List<TipoPrueba> = emptyList(),
    val parametros: List<Parametro> = emptyList(),
    val instrumentosVigentes: List<Instrumento> = emptyList(),

    val pruebas: List<PruebaRegistrada> = emptyList(),
    val formulario: FormularioPrueba = FormularioPrueba(),

    val mensaje: String? = null,
    val errorGeneral: String? = null
) {

    val resultadoGlobal: TipoResultado?
        get() = pruebas.filter { it.realizada }.map { it.resultado }.filterNotNull().maxOrNull()

    val tiposSinRegistro: List<TipoPrueba>
        get() = tiposPrueba.filter { tipo -> pruebas.none { it.nombreTipoPrueba == tipo.nombre } }

    val tipoPruebaElegido: TipoPrueba?
        get() = tiposPrueba.firstOrNull { it.idTipoPrueba == formulario.idTipoPrueba }

    val momentosPermitidos: List<Momento>
        get() = if (visita?.tipoVisita == TipoVisita.PRUEBAS_Y_MANT) listOf(Momento.PREVIA, Momento.POSTERIOR)
        else listOf(Momento.UNICA)

    val instrumentosParaPrueba: List<Instrumento>
        get() = tipoPruebaElegido?.let { InstrumentosPorPrueba.filtrar(it.codigo, instrumentosVigentes) } ?: emptyList()

    val soloUnMinuto: Boolean
        get() = equipo?.let { EvaluadorPrueba.aislacionSoloUnMinuto(it.idTipoEquipo) } ?: false

    val requiereResultadoManual: Boolean
        get() = tipoPruebaElegido?.let {
            EvaluadorPrueba.requiereResultadoManual(it.codigo, equipo?.maxContactosUohm)
        } ?: false

    val parametrosVisibles: List<Parametro>
        get() = parametros.filter { p ->
            when {
                p.codigo == "LECTURA_10MIN" && soloUnMinuto -> false
                p.codigo == "TIEMPO_MEDIDO" && !formulario.opero -> false
                p.codigo == "CORRIENTE_OPERACION" && !formulario.opero -> false
                else -> true
            }
        }
}
