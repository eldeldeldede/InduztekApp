package cl.induztek.app.validacion

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesFormularioTest {

    @Test
    fun correo() {
        assertNotNull(ValidacionesFormulario.correo(""))
        assertNotNull(ValidacionesFormulario.correo("sin-arroba"))
        assertNotNull(ValidacionesFormulario.correo("a@b"))
        assertNull(ValidacionesFormulario.correo("mgodoy@induztek.ejemplo"))
    }

    @Test
    fun tag() {
        assertNull(ValidacionesFormulario.tag("TR-01"))
        assertNull(ValidacionesFormulario.tag("CEL-MT-03"))
        assertNotNull(ValidacionesFormulario.tag(""))
        assertNotNull(ValidacionesFormulario.tag("TR 01"))        // espacio en medio
        assertNotNull(ValidacionesFormulario.tag("A".repeat(21)))
    }

    @Test
    fun anioFabricacion() {
        assertNull(ValidacionesFormulario.anioFabricacion("", 2026))       // opcional
        assertNull(ValidacionesFormulario.anioFabricacion("2012", 2026))
        assertNotNull(ValidacionesFormulario.anioFabricacion("2027", 2026))
        assertNotNull(ValidacionesFormulario.anioFabricacion("1850", 2026))
        assertNotNull(ValidacionesFormulario.anioFabricacion("dos mil", 2026))
    }

    @Test
    fun datosDePlaca() {
        assertNull(ValidacionesFormulario.numeroPositivoOpcional(""))
        assertNull(ValidacionesFormulario.numeroPositivoOpcional("1250,5"))
        assertNotNull(ValidacionesFormulario.numeroPositivoOpcional("0"))
        assertNotNull(ValidacionesFormulario.numeroPositivoOpcional("abc"))
    }

    @Test
    fun condicionesAmbientales() {
        assertNull(ValidacionesFormulario.temperatura("22,5"))
        assertNotNull(ValidacionesFormulario.temperatura(""))
        assertNotNull(ValidacionesFormulario.temperatura("200"))
        assertNull(ValidacionesFormulario.humedad("0"))
        assertNull(ValidacionesFormulario.humedad("100"))
        assertNotNull(ValidacionesFormulario.humedad("100,1"))
        assertNotNull(ValidacionesFormulario.humedad("-1"))
    }

    @Test
    fun medicionYMotivo() {
        assertNull(ValidacionesFormulario.medicion("6100", "la lectura de 1 min"))
        assertNotNull(ValidacionesFormulario.medicion("", "la lectura de 1 min"))
        assertNotNull(ValidacionesFormulario.medicion("-5", "la lectura de 1 min"))
        assertNotNull(ValidacionesFormulario.motivo(""))
        assertNotNull(ValidacionesFormulario.motivo("no"))
        assertNull(ValidacionesFormulario.motivo("Sin acceso al devanado"))
    }

    @Test
    fun ordenDeTrabajoYReceptor() {
        assertNull(ValidacionesFormulario.numeroOt("OT-2026-1187"))
        assertNull(ValidacionesFormulario.numeroOt("ot-2026-1187"))
        assertNotNull(ValidacionesFormulario.numeroOt("1187"))
        assertNotNull(ValidacionesFormulario.nombreReceptor("M"))
        assertNull(ValidacionesFormulario.nombreReceptor("Marcela Fuentes"))
        assertNotNull(ValidacionesFormulario.cargoReceptor(" "))
    }
}
