package cl.induztek.app.validacion

import cl.induztek.app.model.Instrumento
import cl.induztek.app.model.TipoResultado

data class DatosPrueba(
    val valores: Map<String, Double>,
    val maxContactosUohm: Double? = null,
    val soloUnMinuto: Boolean = false,
    val opero: Boolean = true
)

sealed class PruebaElectrica(val codigo: String) {

    abstract val idsInstrumentos: Set<Long>

    abstract val parametroPrincipal: String

    open fun requiereResultadoManual(maxContactosUohm: Double?): Boolean = false

    abstract fun evaluar(datos: DatosPrueba): EvaluadorPrueba.Evaluacion

    open fun errorParametro(codigoParametro: String, nombre: String, texto: String): String? =
        ValidacionesFormulario.medicion(texto, nombre)

    fun esPrincipal(codigoParametro: String): Boolean = parametroPrincipal == codigoParametro

    fun filtrarInstrumentos(vigentes: List<Instrumento>): List<Instrumento> =
        vigentes.filter { it.idInstrumento in idsInstrumentos }

    companion object {
        val todas: List<PruebaElectrica> by lazy {
            listOf(PruebaAislacion, PruebaTtr, PruebaDevanados, PruebaContactos, PruebaOperacionRele)
        }

        fun desdeCodigo(codigo: String): PruebaElectrica? = todas.firstOrNull { it.codigo == codigo }
    }
}

object PruebaAislacion : PruebaElectrica("AISLACION") {
    override val idsInstrumentos = setOf(1L, 2L)
    override val parametroPrincipal = "LECTURA_1MIN"

    override fun errorParametro(codigoParametro: String, nombre: String, texto: String): String? =
        when (codigoParametro) {
            "TENSION_PRUEBA" -> ValidacionesFormulario.medicionEnRango(texto, nombre, 0.25, 15.0, "kV")
            "LECTURA_1MIN" -> ValidacionesFormulario.medicionPositiva(texto, nombre)
            else -> super.errorParametro(codigoParametro, nombre, texto)
        }

    override fun evaluar(datos: DatosPrueba): EvaluadorPrueba.Evaluacion {
        val l1 = datos.valores["LECTURA_1MIN"]
        val l10 = datos.valores["LECTURA_10MIN"]
        return when {
            l1 == null -> EvaluadorPrueba.Evaluacion(null)
            datos.soloUnMinuto ->
                EvaluadorPrueba.Evaluacion(CriteriosResultado.resultadoAislacionSoloUnMinuto(l1))
            l10 == null -> EvaluadorPrueba.Evaluacion(null)
            else -> {
                val ip = CriteriosResultado.indicePolarizacion(l1, l10)
                EvaluadorPrueba.Evaluacion(
                    CriteriosResultado.resultadoAislacion(l1, l10),
                    ip?.let { "IP = ${Unidades.formatear(it)}" }
                )
            }
        }
    }
}

object PruebaTtr : PruebaElectrica("TTR") {
    override val idsInstrumentos = setOf(3L)
    override val parametroPrincipal = "RELACION_MEDIDA"

    override fun errorParametro(codigoParametro: String, nombre: String, texto: String): String? =
        when (codigoParametro) {
            "POSICION_TAP" -> ValidacionesFormulario.posicionCambiador(texto)
            "RELACION_TEORICA", "RELACION_MEDIDA" -> ValidacionesFormulario.medicionPositiva(texto, nombre)
            else -> super.errorParametro(codigoParametro, nombre, texto)
        }

    override fun evaluar(datos: DatosPrueba): EvaluadorPrueba.Evaluacion {
        val teorica = datos.valores["RELACION_TEORICA"]
        val medida = datos.valores["RELACION_MEDIDA"]
        return if (teorica == null || medida == null) EvaluadorPrueba.Evaluacion(null)
        else EvaluadorPrueba.Evaluacion(
            CriteriosResultado.resultadoTtr(teorica, medida),
            CriteriosResultado.errorTtr(teorica, medida)?.let { "Error = ${Unidades.formatear(it)} %" }
        )
    }
}

object PruebaOperacionRele : PruebaElectrica("OPERACION_RELE") {
    override val idsInstrumentos = setOf(5L)
    override val parametroPrincipal = "TIEMPO_MEDIDO"

    override fun errorParametro(codigoParametro: String, nombre: String, texto: String): String? =
        when (codigoParametro) {
            "CORRIENTE_AJUSTE", "TIEMPO_ESPERADO", "TIEMPO_MEDIDO" ->
                ValidacionesFormulario.medicionPositiva(texto, nombre)
            else -> super.errorParametro(codigoParametro, nombre, texto)
        }

    override fun evaluar(datos: DatosPrueba): EvaluadorPrueba.Evaluacion {
        val esperado = datos.valores["TIEMPO_ESPERADO"]
        val medido = datos.valores["TIEMPO_MEDIDO"]
        return when {
            !datos.opero -> EvaluadorPrueba.Evaluacion(TipoResultado.RECHAZADO, "El relé no operó")
            esperado == null || medido == null -> EvaluadorPrueba.Evaluacion(null)
            else -> EvaluadorPrueba.Evaluacion(
                CriteriosResultado.resultadoRele(esperado, medido, true),
                CriteriosResultado.diferenciaTiempo(esperado, medido)
                    ?.let { "Diferencia = ${Unidades.formatear(it, 1)} %" }
            )
        }
    }
}

sealed class PruebaDeResistencia(codigo: String) : PruebaElectrica(codigo) {
    override val idsInstrumentos = setOf(4L)

    override fun errorParametro(codigoParametro: String, nombre: String, texto: String): String? =
        if (codigoParametro == parametroPrincipal) ValidacionesFormulario.medicionPositiva(texto, nombre)
        else super.errorParametro(codigoParametro, nombre, texto)
}

object PruebaDevanados : PruebaDeResistencia("DEVANADOS") {
    override val parametroPrincipal = "RESISTENCIA"

    override fun requiereResultadoManual(maxContactosUohm: Double?): Boolean = true

    override fun evaluar(datos: DatosPrueba): EvaluadorPrueba.Evaluacion = EvaluadorPrueba.Evaluacion(null)
}

object PruebaContactos : PruebaDeResistencia("CONTACTOS") {
    override val parametroPrincipal = "RESISTENCIA_CONTACTO"

    override fun requiereResultadoManual(maxContactosUohm: Double?): Boolean = maxContactosUohm == null

    override fun evaluar(datos: DatosPrueba): EvaluadorPrueba.Evaluacion {
        val valor = datos.valores["RESISTENCIA_CONTACTO"]
        val maximo = datos.maxContactosUohm
        return if (valor == null || maximo == null) EvaluadorPrueba.Evaluacion(null)
        else EvaluadorPrueba.Evaluacion(
            CriteriosResultado.resultadoContactos(valor, maximo),
            CriteriosResultado.porcentajeSobreMaximo(valor, maximo)
                ?.let { "${Unidades.formatear(it, 1)} % del máximo de placa" }
        )
    }
}
