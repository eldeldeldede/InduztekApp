package cl.induztek.app.validacion

import cl.induztek.app.model.Instrumento

object InstrumentosPorPrueba {

    fun filtrar(codigoTipoPrueba: String, vigentes: List<Instrumento>): List<Instrumento> =
        PruebaElectrica.desdeCodigo(codigoTipoPrueba)?.filtrarInstrumentos(vigentes) ?: emptyList()
}
