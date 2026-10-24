package cl.induztek.app.validacion

import cl.induztek.app.model.Instrumento

object InstrumentosPorPrueba {

    private val IDS_POR_TIPO_PRUEBA: Map<String, Set<Long>> = mapOf(
        "AISLACION" to setOf(1L, 2L),
        "TTR" to setOf(3L),
        "DEVANADOS" to setOf(4L),
        "CONTACTOS" to setOf(4L),
        "OPERACION_RELE" to setOf(5L)
    )

    fun idsPermitidos(codigoTipoPrueba: String): Set<Long> =
        IDS_POR_TIPO_PRUEBA[codigoTipoPrueba] ?: emptySet()

    fun filtrar(codigoTipoPrueba: String, vigentes: List<Instrumento>): List<Instrumento> {
        val permitidos = idsPermitidos(codigoTipoPrueba)
        return vigentes.filter { it.idInstrumento in permitidos }
    }
}
