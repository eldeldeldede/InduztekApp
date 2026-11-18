package cl.induztek.app.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorRutTest {

    @Test
    fun rutsDelAnexo9SonValidos() {
        assertTrue(ValidadorRut.esValido("76.482.915-8"))
        assertTrue(ValidadorRut.esValido("96.731.540-0"))
        assertTrue(ValidadorRut.esValido("77.105.362-9"))
    }

    @Test
    fun rechazaDigitoVerificadorIncorrecto() {
        assertFalse(ValidadorRut.esValido("76.482.915-9"))
        assertFalse(ValidadorRut.esValido("96.731.540-1"))
    }

    @Test
    fun formateaConPuntosYGuion() {
        assertEquals("76.482.915-8", ValidadorRut.formatear("764829158"))
        assertEquals("96.731.540-0", ValidadorRut.formatear("96731540-0"))
    }
}
