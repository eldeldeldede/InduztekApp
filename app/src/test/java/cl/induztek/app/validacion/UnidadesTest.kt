package cl.induztek.app.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UnidadesTest {

    @Test
    fun cincoComaOchoGigaohmEs5800Megaohm() {
        // Ejemplo exacto del MER y del Anexo 12 ("5,8 G")
        assertEquals(5800.0, Unidades.aMegaOhm(5.8, UnidadAislacion.GOHM), 1e-9)
    }

    @Test
    fun convierteOtrasUnidadesDeAislacion() {
        assertEquals(6100.0, Unidades.aMegaOhm(6100.0, UnidadAislacion.MOHM), 1e-9)
        assertEquals(0.5, Unidades.aMegaOhm(500.0, UnidadAislacion.KOHM), 1e-9)
        assertEquals(2_000_000.0, Unidades.aMegaOhm(2.0, UnidadAislacion.TOHM), 1e-9)
    }

    @Test
    fun convierteContactosAMicroohm() {
        assertEquals(85.0, Unidades.aMicroOhm(0.085, UnidadBaja.MOHM), 1e-9)
        assertEquals(120.0, Unidades.aMicroOhm(120.0, UnidadBaja.UOHM), 1e-9)
    }

    @Test
    fun convierteDevanadosAMiliohm() {
        // Anexo 12: "1,85" y "1842" en la misma fila -> 1,85 Ω = 1850 mΩ; 1842 mΩ queda igual
        assertEquals(1850.0, Unidades.aMiliOhm(1.85, UnidadBaja.OHM), 1e-6)
        assertEquals(1842.0, Unidades.aMiliOhm(1842.0, UnidadBaja.MOHM), 1e-6)
    }

    @Test
    fun parseaComaYPunto() {
        assertEquals(5.8, Unidades.parsearDecimal("5,8")!!, 1e-9)
        assertEquals(5.8, Unidades.parsearDecimal(" 5.8 ")!!, 1e-9)
        assertEquals(-3.0, Unidades.parsearDecimal("-3")!!, 1e-9)
    }

    @Test
    fun rechazaTextoQueNoEsNumero() {
        assertNull(Unidades.parsearDecimal(""))
        assertNull(Unidades.parsearDecimal("abc"))
        assertNull(Unidades.parsearDecimal("NaN"))
        assertNull(Unidades.parsearDecimal("Infinity"))
    }

    @Test
    fun formateaSinCerosSobrantes() {
        assertEquals("5800", Unidades.formatear(5800.0))
        assertEquals("1,8", Unidades.formatear(1.8))
        assertEquals("1,93", Unidades.formatear(1.9333))
    }

    @Test
    fun formateaPorcentajeConSigno() {
        assertEquals("-37,8 %", Unidades.formatearPorcentaje(-37.755))
        assertEquals("+12,0 %", Unidades.formatearPorcentaje(12.0))
    }
}
