package cl.induztek.app.validacion

import cl.induztek.app.model.TipoResultado

object EvaluadorPrueba {

    data class Evaluacion(val resultado: TipoResultado?, val detalle: String? = null)

    fun requiereResultadoManual(codigoTipoPrueba: String, maxContactosUohm: Double?): Boolean =
        PruebaElectrica.desdeCodigo(codigoTipoPrueba)?.requiereResultadoManual(maxContactosUohm) ?: false

    fun aislacionSoloUnMinuto(idTipoEquipo: Long): Boolean = idTipoEquipo == 2L || idTipoEquipo == 3L

    fun evaluar(
        codigoTipoPrueba: String,
        valores: Map<String, Double>,
        maxContactosUohm: Double? = null,
        soloUnMinuto: Boolean = false,
        opero: Boolean = true
    ): Evaluacion = PruebaElectrica.desdeCodigo(codigoTipoPrueba)
        ?.evaluar(DatosPrueba(valores, maxContactosUohm, soloUnMinuto, opero))
        ?: Evaluacion(null)
}
