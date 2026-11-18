package cl.induztek.app.validacion

import cl.induztek.app.model.TipoResultado.APROBADO
import cl.induztek.app.model.TipoResultado.OBSERVADO
import cl.induztek.app.model.TipoResultado.RECHAZADO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CriteriosResultadoTest {

    // ---- RN-06: aislación. Datos reales del TR-01 (Anexo 9), lectura 1 min / 10 min ----
    @Test
    fun tr01_2024_ip25Aprobado() {
        assertEquals(2.5, CriteriosResultado.indicePolarizacion(12500.0, 31250.0)!!, 1e-9)
        assertEquals(APROBADO, CriteriosResultado.resultadoAislacion(12500.0, 31250.0))
    }

    @Test
    fun tr01_2025_ip22Aprobado() {
        assertEquals(2.2, CriteriosResultado.indicePolarizacion(9800.0, 21560.0)!!, 1e-9)
        assertEquals(APROBADO, CriteriosResultado.resultadoAislacion(9800.0, 21560.0))
    }

    @Test
    fun tr01_2026_ip18Observado() {
        assertEquals(1.8, CriteriosResultado.indicePolarizacion(6100.0, 10980.0)!!, 1e-9)
        assertEquals(OBSERVADO, CriteriosResultado.resultadoAislacion(6100.0, 10980.0))
    }

    @Test
    fun aislacionBordesDelIp() {
        assertEquals(APROBADO, CriteriosResultado.resultadoAislacion(1000.0, 2000.0))   // IP = 2,0
        assertEquals(OBSERVADO, CriteriosResultado.resultadoAislacion(1000.0, 1000.0))  // IP = 1,0
        assertEquals(RECHAZADO, CriteriosResultado.resultadoAislacion(1000.0, 990.0))   // IP < 1,0
    }

    @Test
    fun lecturaMenorA100MohmRechazaAunConIpBueno() {
        assertEquals(RECHAZADO, CriteriosResultado.resultadoAislacion(99.0, 400.0))  // IP 4,0 pero 99 MΩ
        assertEquals(OBSERVADO, CriteriosResultado.resultadoAislacion(100.0, 150.0)) // 100 MΩ justo no rechaza
    }

    @Test
    fun aislacionConDatosInvalidosNoSeEvalua() {
        assertNull(CriteriosResultado.indicePolarizacion(0.0, 100.0))
        assertNull(CriteriosResultado.resultadoAislacion(0.0, 100.0))
        assertNull(CriteriosResultado.resultadoAislacion(-5.0, 100.0))
    }

    @Test
    fun aislacionSoloUnMinuto() {
        assertEquals(APROBADO, CriteriosResultado.resultadoAislacionSoloUnMinuto(100.0))
        assertEquals(RECHAZADO, CriteriosResultado.resultadoAislacionSoloUnMinuto(99.9))
    }

    // ---- RN-07: TTR ----
    @Test
    fun ttrErrorYBordes() {
        assertEquals(0.5, CriteriosResultado.errorTtr(100.0, 100.5)!!, 1e-9)
        assertEquals(APROBADO, CriteriosResultado.resultadoTtr(100.0, 100.5))   // 0,5 % justo
        assertEquals(OBSERVADO, CriteriosResultado.resultadoTtr(100.0, 100.51)) // > 0,5 %
        assertEquals(OBSERVADO, CriteriosResultado.resultadoTtr(100.0, 99.0))   // 1,0 % justo, valor absoluto
        assertEquals(RECHAZADO, CriteriosResultado.resultadoTtr(100.0, 98.9))   // > 1,0 %
        assertNull(CriteriosResultado.resultadoTtr(0.0, 5.0))
    }

    // ---- RN-08: contactos ----
    @Test
    fun contactosContraMaximoDePlaca() {
        assertEquals(APROBADO, CriteriosResultado.resultadoContactos(100.0, 100.0))   // 100 %
        assertEquals(OBSERVADO, CriteriosResultado.resultadoContactos(101.0, 100.0))  // > 100 %
        assertEquals(OBSERVADO, CriteriosResultado.resultadoContactos(120.0, 100.0))  // 120 % justo
        assertEquals(RECHAZADO, CriteriosResultado.resultadoContactos(121.0, 100.0))
        assertNull(CriteriosResultado.resultadoContactos(50.0, 0.0))
    }

    // ---- RN-08b: relé ----
    @Test
    fun releTiempos() {
        assertEquals(APROBADO, CriteriosResultado.resultadoRele(100.0, 105.0))   // 5 % justo
        assertEquals(OBSERVADO, CriteriosResultado.resultadoRele(100.0, 106.0))
        assertEquals(OBSERVADO, CriteriosResultado.resultadoRele(100.0, 90.0))   // 10 % justo, también por debajo
        assertEquals(RECHAZADO, CriteriosResultado.resultadoRele(100.0, 111.0))
    }

    @Test
    fun releQueNoOperaSiempreRechaza() {
        assertEquals(RECHAZADO, CriteriosResultado.resultadoRele(100.0, 0.0, opero = false))
    }

    // ---- RN-09: variación ----
    @Test
    fun variacionDelTr01() {
        // 12500 -> 9800 = -21,6 %  (no significativa)   |   9800 -> 6100 = -37,8 % (significativa)
        assertEquals(-21.6, CriteriosResultado.variacionPorcentual(12500.0, 9800.0)!!, 1e-9)
        assertFalse(CriteriosResultado.esVariacionSignificativa(12500.0, 9800.0))
        assertEquals(-37.755, CriteriosResultado.variacionPorcentual(9800.0, 6100.0)!!, 1e-3)
        assertTrue(CriteriosResultado.esVariacionSignificativa(9800.0, 6100.0))
    }

    @Test
    fun variacionExactamente25NoEsSignificativaPeroSubirMuchoSi() {
        assertFalse(CriteriosResultado.esVariacionSignificativa(100.0, 75.0))  // -25 % justo
        assertFalse(CriteriosResultado.esVariacionSignificativa(100.0, 125.0)) // +25 % justo
        assertTrue(CriteriosResultado.esVariacionSignificativa(100.0, 126.0))  // sube > 25 %
        assertFalse(CriteriosResultado.esVariacionSignificativa(0.0, 50.0))    // sin base de comparación
    }

    // ---- RN-10: resultado global ----
    @Test
    fun resultadoGlobalEsElPeor() {
        assertEquals(RECHAZADO, CriteriosResultado.resultadoGlobal(listOf(APROBADO, RECHAZADO, OBSERVADO)))
        assertEquals(OBSERVADO, CriteriosResultado.resultadoGlobal(listOf(APROBADO, OBSERVADO)))
        assertEquals(APROBADO, CriteriosResultado.resultadoGlobal(listOf(APROBADO, APROBADO)))
    }

    @Test
    fun pruebasNoRealizadasNoCuentan() {
        // null = prueba "No realizada" (Aclaración 2)
        assertEquals(APROBADO, CriteriosResultado.resultadoGlobal(listOf(APROBADO, null)))
        assertNull(CriteriosResultado.resultadoGlobal(listOf(null, null)))
        assertNull(CriteriosResultado.resultadoGlobal(emptyList()))
    }
}
