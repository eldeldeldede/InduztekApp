package cl.induztek.app.validacion

import cl.induztek.app.model.TipoResultado.APROBADO
import cl.induztek.app.model.TipoResultado.OBSERVADO
import cl.induztek.app.model.TipoResultado.RECHAZADO
import org.junit.Assert.assertEquals
import org.junit.Test

class CriteriosResultadoTest {

    @Test
    fun tr01_2026_ip18Observado() {
        assertEquals(1.8, CriteriosResultado.indicePolarizacion(6100.0, 10980.0)!!, 1e-9)
        assertEquals(OBSERVADO, CriteriosResultado.resultadoAislacion(6100.0, 10980.0))
    }

    @Test
    fun aislacionBordesDelIp() {
        assertEquals(APROBADO, CriteriosResultado.resultadoAislacion(1000.0, 2000.0))
        assertEquals(OBSERVADO, CriteriosResultado.resultadoAislacion(1000.0, 1000.0))
        assertEquals(RECHAZADO, CriteriosResultado.resultadoAislacion(1000.0, 990.0))
    }

    @Test
    fun resultadoGlobalEsElPeor() {
        assertEquals(RECHAZADO, CriteriosResultado.resultadoGlobal(listOf(APROBADO, RECHAZADO, OBSERVADO)))
        assertEquals(OBSERVADO, CriteriosResultado.resultadoGlobal(listOf(APROBADO, OBSERVADO)))
        assertEquals(APROBADO, CriteriosResultado.resultadoGlobal(listOf(APROBADO, APROBADO)))
    }

    @Test
    fun valoresGrandesSeMuestranConSeparadorDeMiles() {
        assertEquals("1.500.000.000", Unidades.formatearLegible(1500000000.0))
        assertEquals("1.350", Unidades.formatearLegible(1350.0))
        assertEquals("33,05", Unidades.formatearLegible(33.05))
        assertEquals("812,4", Unidades.formatearLegible(812.4))
    }
}
