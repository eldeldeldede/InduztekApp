package cl.induztek.app.validacion

import cl.induztek.app.model.TipoResultado
import kotlin.math.abs

object CriteriosResultado {

    fun indicePolarizacion(lectura1Min: Double, lectura10Min: Double): Double? =
        if (lectura1Min > 0.0 && lectura10Min >= 0.0) lectura10Min / lectura1Min else null

    fun resultadoAislacion(lectura1Min: Double, lectura10Min: Double): TipoResultado? {
        val ip = indicePolarizacion(lectura1Min, lectura10Min) ?: return null
        return when {
            lectura1Min < 100.0 || ip < 1.0 -> TipoResultado.RECHAZADO
            ip < 2.0 -> TipoResultado.OBSERVADO
            else -> TipoResultado.APROBADO
        }
    }

    fun resultadoAislacionSoloUnMinuto(lectura1Min: Double): TipoResultado? =
        if (lectura1Min < 0.0) null
        else if (lectura1Min < 100.0) TipoResultado.RECHAZADO else TipoResultado.APROBADO

    fun errorTtr(teorica: Double, medida: Double): Double? =
        if (teorica > 0.0 && medida >= 0.0) abs(medida - teorica) / teorica * 100.0 else null

    fun resultadoTtr(teorica: Double, medida: Double): TipoResultado? {
        val error = errorTtr(teorica, medida) ?: return null
        return when {
            error <= 0.5 -> TipoResultado.APROBADO
            error <= 1.0 -> TipoResultado.OBSERVADO
            else -> TipoResultado.RECHAZADO
        }
    }

    fun porcentajeSobreMaximo(valor: Double, maximoPlaca: Double): Double? =
        if (maximoPlaca > 0.0 && valor >= 0.0) valor / maximoPlaca * 100.0 else null

    fun resultadoContactos(valor: Double, maximoPlaca: Double): TipoResultado? {
        val pct = porcentajeSobreMaximo(valor, maximoPlaca) ?: return null
        return when {
            pct <= 100.0 -> TipoResultado.APROBADO
            pct <= 120.0 -> TipoResultado.OBSERVADO
            else -> TipoResultado.RECHAZADO
        }
    }

    fun diferenciaTiempo(esperadoMs: Double, medidoMs: Double): Double? =
        if (esperadoMs > 0.0 && medidoMs >= 0.0) abs(medidoMs - esperadoMs) / esperadoMs * 100.0 else null

    fun resultadoRele(esperadoMs: Double, medidoMs: Double, opero: Boolean = true): TipoResultado? {
        if (!opero) return TipoResultado.RECHAZADO
        val diferencia = diferenciaTiempo(esperadoMs, medidoMs) ?: return null
        return when {
            diferencia <= 5.0 -> TipoResultado.APROBADO
            diferencia <= 10.0 -> TipoResultado.OBSERVADO
            else -> TipoResultado.RECHAZADO
        }
    }

    const val UMBRAL_VARIACION_PCT = 25.0

    fun variacionPorcentual(anterior: Double, actual: Double): Double? =
        if (anterior != 0.0) (actual - anterior) / anterior * 100.0 else null

    fun esVariacionSignificativa(anterior: Double, actual: Double): Boolean {
        val variacion = variacionPorcentual(anterior, actual) ?: return false
        return abs(variacion) > UMBRAL_VARIACION_PCT
    }

    fun resultadoGlobal(resultados: List<TipoResultado?>): TipoResultado? =
        resultados.filterNotNull().maxOrNull()
}
