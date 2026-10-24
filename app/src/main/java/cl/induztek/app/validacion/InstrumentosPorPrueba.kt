package cl.induztek.app.validacion

import cl.induztek.app.model.Instrumento

object InstrumentosPorPrueba {

    fun idsPermitidos(codigoTipoPrueba: String): Set<Long> =
        PruebaElectrica.desdeCodigo(codigoTipoPrueba)?.idsInstrumentos ?: emptySet()

    fun filtrar(codigoTipoPrueba: String, vigentes: List<Instrumento>): List<Instrumento> =
        PruebaElectrica.desdeCodigo(codigoTipoPrueba)?.filtrarInstrumentos(vigentes) ?: emptyList()
}
