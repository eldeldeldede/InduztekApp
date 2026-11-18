package cl.induztek.app.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesRecuperacionTest {

    @Test
    fun `un codigo de 6 digitos es valido`() {
        assertNull(ValidacionesFormulario.codigoRecuperacion("123456"))
        assertNull(ValidacionesFormulario.codigoRecuperacion(" 004821 "))
    }

    @Test
    fun `un codigo vacio pide ingresarlo`() {
        assertEquals("Ingresa el código que te llegó por correo", ValidacionesFormulario.codigoRecuperacion("  "))
    }

    @Test
    fun `un codigo con letras o de otro largo se rechaza`() {
        assertNotNull(ValidacionesFormulario.codigoRecuperacion("12345"))
        assertNotNull(ValidacionesFormulario.codigoRecuperacion("1234567"))
        assertNotNull(ValidacionesFormulario.codigoRecuperacion("12a456"))
    }

    @Test
    fun `la clave nueva de la recuperacion exige 6 caracteres`() {
        assertNotNull(ValidacionesFormulario.claveNueva("abc", ""))
        assertNull(ValidacionesFormulario.claveNueva("abcdef", ""))
    }
}
