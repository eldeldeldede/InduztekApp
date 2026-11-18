package cl.induztek.app.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesClaveYUsuarioTest {

    @Test
    fun claveNueva_exigeMinimoSeisYDistintaDeLaActual() {
        assertNotNull(ValidacionesFormulario.claveNueva("", "Temporal1"))
        assertNotNull(ValidacionesFormulario.claveNueva("abc12", "Temporal1"))
        assertEquals("Debe ser distinta de la contraseña actual", ValidacionesFormulario.claveNueva("Temporal1", "Temporal1"))
        assertNull(ValidacionesFormulario.claveNueva("NuevaClave9", "Temporal1"))
    }

    @Test
    fun confirmacionClave_debeCoincidir() {
        assertNotNull(ValidacionesFormulario.confirmacionClave("NuevaClave9", ""))
        assertNotNull(ValidacionesFormulario.confirmacionClave("NuevaClave9", "otra"))
        assertNull(ValidacionesFormulario.confirmacionClave("NuevaClave9", "NuevaClave9"))
    }

    @Test
    fun nombreUsuario_minimoTresCaracteres() {
        assertNotNull(ValidacionesFormulario.nombreUsuario(" a "))
        assertNull(ValidacionesFormulario.nombreUsuario("Ana"))
    }

    @Test
    fun telefono_esOpcionalPeroSiVieneDebeSerValido() {
        assertNull(ValidacionesFormulario.telefono(""))
        assertNull(ValidacionesFormulario.telefono("+56 9 1234 5678"))
        assertNotNull(ValidacionesFormulario.telefono("123"))
        assertNotNull(ValidacionesFormulario.telefono("abcdefghij"))
    }
}
