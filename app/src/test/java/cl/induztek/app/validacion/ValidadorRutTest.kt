package cl.induztek.app.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Test unitario normal (corre en el computador, sin emulador): botón verde junto a la clase.
class ValidadorRutTest {

    @Test
    fun rutsDelAnexo9SonValidos() {
        // Clientes del Anexo 9: "tienen dígito verificador válido para poder aplicar la RN-03"
        assertTrue(ValidadorRut.esValido("76.482.915-8"))
        assertTrue(ValidadorRut.esValido("96.731.540-0"))
        assertTrue(ValidadorRut.esValido("77.105.362-9"))
    }

    @Test
    fun aceptaConYSinPuntosYGuion() {
        assertTrue(ValidadorRut.esValido("76482915-8"))
        assertTrue(ValidadorRut.esValido("764829158"))
    }

    @Test
    fun rechazaDigitoVerificadorIncorrecto() {
        assertFalse(ValidadorRut.esValido("76.482.915-9"))
        assertFalse(ValidadorRut.esValido("96.731.540-1"))
    }

    @Test
    fun digitoKSeCalculaYSeAcepta() {
        // 10.000.013 tiene DV K (comprobado a mano: suma=..., 11 - resto = 10 -> K)
        val dv = ValidadorRut.calcularDigito("10000013")
        assertEquals('K', dv)
        assertTrue(ValidadorRut.esValido("10.000.013-K"))
        assertTrue(ValidadorRut.esValido("10.000.013-k")) // minúscula también
    }

    @Test
    fun rechazaTextosMalFormados() {
        assertFalse(ValidadorRut.esValido(""))
        assertFalse(ValidadorRut.esValido("-"))
        assertFalse(ValidadorRut.esValido("abc"))
        assertFalse(ValidadorRut.esValido("1-9"))         // cuerpo demasiado corto
        assertFalse(ValidadorRut.esValido("1K234567-8"))  // K en medio
    }

    @Test
    fun formateaConPuntosYGuion() {
        assertEquals("76.482.915-8", ValidadorRut.formatear("764829158"))
        assertEquals("96.731.540-0", ValidadorRut.formatear("96731540-0"))
    }

    @Test
    fun errorDevuelveMensajeONulo() {
        assertNotNull(ValidadorRut.error(""))
        assertNotNull(ValidadorRut.error("76.482.915-0"))
        assertNull(ValidadorRut.error("76.482.915-8"))
    }
}
