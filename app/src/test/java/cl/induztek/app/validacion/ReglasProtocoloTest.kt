package cl.induztek.app.validacion

import cl.induztek.app.model.CodigoRol.CLIENTE
import cl.induztek.app.model.CodigoRol.INGENIERO
import cl.induztek.app.model.CodigoRol.JEFE_TECNICO
import cl.induztek.app.model.CodigoRol.TECNICO
import cl.induztek.app.model.TipoEstadoProtocolo.ANULADO
import cl.induztek.app.model.TipoEstadoProtocolo.BORRADOR
import cl.induztek.app.model.TipoEstadoProtocolo.FORMALIZADO
import cl.induztek.app.model.TipoEstadoProtocolo.OBSERVADO
import cl.induztek.app.model.TipoEstadoProtocolo.REGISTRADO_TERRENO
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasProtocoloTest {

    private fun error(desde: cl.induztek.app.model.TipoEstadoProtocolo,
                      hacia: cl.induztek.app.model.TipoEstadoProtocolo,
                      rol: cl.induztek.app.model.CodigoRol,
                      motivo: String? = null) =
        ReglasProtocolo.errorTransicion(desde, hacia, rol, motivo)

    // ---- RN-04 ----
    @Test
    fun borradorARegistradoLoHacenTecnicoEIngeniero() {
        assertNull(error(BORRADOR, REGISTRADO_TERRENO, TECNICO))
        assertNull(error(BORRADOR, REGISTRADO_TERRENO, INGENIERO))
        assertNotNull(error(BORRADOR, REGISTRADO_TERRENO, JEFE_TECNICO))
        assertNotNull(error(BORRADOR, REGISTRADO_TERRENO, CLIENTE))
    }

    @Test
    fun registrarExigePruebaCompletaYCondicionesAmbientales() {
        assertNotNull(ReglasProtocolo.errorTransicion(BORRADOR, REGISTRADO_TERRENO, TECNICO, tienePruebaCompleta = false))
        assertNotNull(ReglasProtocolo.errorTransicion(BORRADOR, REGISTRADO_TERRENO, TECNICO, tieneCondicionesAmbientales = false))
    }

    @Test
    fun formalizarSoloIngenieroOJefe() {
        assertNull(error(REGISTRADO_TERRENO, FORMALIZADO, INGENIERO))
        assertNull(error(REGISTRADO_TERRENO, FORMALIZADO, JEFE_TECNICO))
        assertNotNull(error(REGISTRADO_TERRENO, FORMALIZADO, TECNICO))
        assertNotNull(error(BORRADOR, FORMALIZADO, INGENIERO)) // no se salta pasos
    }

    @Test
    fun formalizarConVariacionSinComentarioSeBloquea() {
        assertNotNull(ReglasProtocolo.errorTransicion(REGISTRADO_TERRENO, FORMALIZADO, INGENIERO, faltaComentarioVariacion = true))
    }

    @Test
    fun observarYAnularExigenMotivo() {
        assertNotNull(error(REGISTRADO_TERRENO, OBSERVADO, INGENIERO, motivo = null))
        assertNotNull(error(REGISTRADO_TERRENO, OBSERVADO, INGENIERO, motivo = "   "))
        assertNull(error(REGISTRADO_TERRENO, OBSERVADO, INGENIERO, motivo = "Falta medición BT"))
        assertNotNull(error(REGISTRADO_TERRENO, OBSERVADO, TECNICO, motivo = "x"))
        assertNotNull(error(BORRADOR, ANULADO, JEFE_TECNICO, motivo = null))
        assertNull(error(BORRADOR, ANULADO, JEFE_TECNICO, motivo = "Equipo equivocado"))
    }

    @Test
    fun observadoVuelveARegistradoTrasCorregir() {
        assertNull(error(OBSERVADO, REGISTRADO_TERRENO, TECNICO))
    }

    @Test
    fun formalizadoYAnuladoSonDefinitivos() {
        for (hacia in cl.induztek.app.model.TipoEstadoProtocolo.entries) {
            assertNotNull(error(FORMALIZADO, hacia, JEFE_TECNICO, motivo = "x"))
            assertNotNull(error(ANULADO, hacia, JEFE_TECNICO, motivo = "x"))
        }
    }

    @Test
    fun nadieVuelveABorrador() {
        assertNotNull(error(REGISTRADO_TERRENO, BORRADOR, INGENIERO))
    }

    @Test
    fun estadosPosiblesPorRol() {
        assertEquals(listOf(REGISTRADO_TERRENO), ReglasProtocolo.estadosPosibles(BORRADOR, TECNICO))
        assertEquals(listOf(REGISTRADO_TERRENO, ANULADO), ReglasProtocolo.estadosPosibles(BORRADOR, INGENIERO))
        assertEquals(listOf(OBSERVADO, FORMALIZADO, ANULADO).sorted(), ReglasProtocolo.estadosPosibles(REGISTRADO_TERRENO, JEFE_TECNICO).sorted())
        assertTrue(ReglasProtocolo.estadosPosibles(FORMALIZADO, JEFE_TECNICO).isEmpty())
        assertTrue(ReglasProtocolo.estadosPosibles(BORRADOR, CLIENTE).isEmpty())
    }

    // ---- RN-17 ----
    @Test
    fun tecnicoSoloModificaLoSuyoEnBorradorUObservado() {
        assertTrue(ReglasProtocolo.puedeModificar(TECNICO, 3, 3, BORRADOR))
        assertTrue(ReglasProtocolo.puedeModificar(TECNICO, 3, 3, OBSERVADO))
        assertFalse(ReglasProtocolo.puedeModificar(TECNICO, 3, 3, REGISTRADO_TERRENO))
        assertFalse(ReglasProtocolo.puedeModificar(TECNICO, 3, 4, BORRADOR)) // de otro técnico
    }

    @Test
    fun ingenieroNoModificaFormalizadosNiAnulados() {
        assertTrue(ReglasProtocolo.puedeModificar(INGENIERO, 2, 3, REGISTRADO_TERRENO))
        assertFalse(ReglasProtocolo.puedeModificar(INGENIERO, 2, 3, FORMALIZADO))
        assertFalse(ReglasProtocolo.puedeModificar(JEFE_TECNICO, 1, 3, ANULADO))
        assertFalse(ReglasProtocolo.puedeModificar(CLIENTE, 8, 8, BORRADOR))
    }

    // ---- RN-12 ----
    @Test
    fun soloElJefeGestionaInstrumentos() {
        assertTrue(ReglasProtocolo.puedeGestionarInstrumentos(JEFE_TECNICO))
        assertFalse(ReglasProtocolo.puedeGestionarInstrumentos(INGENIERO))
        assertFalse(ReglasProtocolo.puedeGestionarInstrumentos(TECNICO))
    }

    // ---- RN-13 ----
    @Test
    fun cierreDeVisita() {
        assertNull(ReglasProtocolo.errorCierreVisita(listOf(REGISTRADO_TERRENO, ANULADO)))
        assertNotNull(ReglasProtocolo.errorCierreVisita(listOf(REGISTRADO_TERRENO, BORRADOR)))
        assertNotNull(ReglasProtocolo.errorCierreVisita(listOf(OBSERVADO)))
        assertNotNull(ReglasProtocolo.errorCierreVisita(emptyList()))
    }

    // ---- RN-16 ----
    @Test
    fun diasHabiles() {
        val viernes = LocalDate.of(2026, 10, 16) // viernes
        assertEquals(DayOfWeekCheck.VIERNES, DayOfWeekCheck.de(viernes))
        assertEquals(0, ReglasProtocolo.diasHabilesEntre(viernes, viernes))
        assertEquals(0, ReglasProtocolo.diasHabilesEntre(viernes, LocalDate.of(2026, 10, 18))) // fin de semana
        assertEquals(1, ReglasProtocolo.diasHabilesEntre(viernes, LocalDate.of(2026, 10, 19))) // lunes
        assertEquals(2, ReglasProtocolo.diasHabilesEntre(viernes, LocalDate.of(2026, 10, 20))) // martes
        assertEquals(3, ReglasProtocolo.diasHabilesEntre(viernes, LocalDate.of(2026, 10, 21))) // miércoles
    }

    @Test
    fun atrasado() {
        val viernes = LocalDate.of(2026, 10, 16)
        assertFalse(ReglasProtocolo.estaAtrasado(viernes, LocalDate.of(2026, 10, 20), REGISTRADO_TERRENO)) // 2 días: aún en plazo
        assertTrue(ReglasProtocolo.estaAtrasado(viernes, LocalDate.of(2026, 10, 21), REGISTRADO_TERRENO))  // 3 días
        assertFalse(ReglasProtocolo.estaAtrasado(viernes, LocalDate.of(2026, 10, 30), FORMALIZADO))
        assertFalse(ReglasProtocolo.estaAtrasado(viernes, LocalDate.of(2026, 10, 30), ANULADO))
    }
}

// Pequeña ayuda del test para no depender del día de la semana "a ojo".
private object DayOfWeekCheck {
    const val VIERNES = "FRIDAY"
    fun de(fecha: LocalDate) = fecha.dayOfWeek.name
}
