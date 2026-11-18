package cl.induztek.app.local

import cl.induztek.app.model.Parametro
import cl.induztek.app.model.TipoPrueba
import cl.induztek.app.network.MedicionSolicitud
import cl.induztek.app.network.PruebaSolicitud
import cl.induztek.app.network.VisitaDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocoloLocalTest {

    private val tipo = TipoPrueba(7, "AISLACION", "Aislación")
    private val parametros = listOf(Parametro(1, 7, "LECTURA_1MIN", "Lectura 1 min", "MΩ"))

    private fun solicitud(elemento: String = "AT-T") = PruebaSolicitud(
        7, elemento, "UNICA", true, null, 2, null, null, null, listOf(MedicionSolicitud("LECTURA_1MIN", 1500.0))
    )

    private fun base() = nuevoProtocoloLocal(-1, 5, "OT-1", 10, 3, "2026-11-18T10:00:00")

    @Test
    fun `un protocolo creado sin conexion parte en borrador con numero provisorio`() {
        val p = base()
        assertEquals("PEND-1", p.numero)
        assertEquals("BORRADOR", p.estado)
        assertTrue(p.pruebas!!.isEmpty())
    }

    @Test
    fun `la prueba local muestra el nombre y la unidad del parametro`() {
        val prueba = pruebaLocal(-2, solicitud(), ContextoPruebaLocal(tipo, parametros, "APROBADO"))
        assertEquals("Aislación", prueba.tipoPruebaNombre)
        assertEquals("Lectura 1 min", prueba.mediciones!!.first().parametroNombre)
        assertEquals("MΩ", prueba.mediciones!!.first().unidad)
        assertEquals("APROBADO", prueba.resultado)
    }

    @Test
    fun `una prueba no realizada no guarda resultado`() {
        val s = solicitud().copy(realizada = false, motivoNoRealizada = "Sin acceso", mediciones = emptyList())
        val prueba = pruebaLocal(-3, s, ContextoPruebaLocal(tipo, parametros, "APROBADO"))
        assertNull(prueba.resultado)
    }

    @Test
    fun `agregar y quitar pruebas`() {
        val prueba = pruebaLocal(-2, solicitud(), ContextoPruebaLocal(tipo, parametros, "APROBADO"))
        val con = base().conPrueba(prueba)
        assertEquals(1, con.pruebas!!.size)
        assertTrue(con.sinPrueba(-2).pruebas!!.isEmpty())
    }

    @Test
    fun `registrar en terreno toma el peor resultado y deja historial`() {
        val ok = pruebaLocal(-2, solicitud("AT-T"), ContextoPruebaLocal(tipo, parametros, "APROBADO"))
        val mal = pruebaLocal(-3, solicitud("BT-T"), ContextoPruebaLocal(tipo, parametros, "RECHAZADO"))
        val registrado = base().conPrueba(ok).conPrueba(mal).registradoEnTerreno(3, "2026-11-18T11:00:00", -4)
        assertEquals("REGISTRADO_TERRENO", registrado.estado)
        assertEquals("RECHAZADO", registrado.resultadoGlobal)
        assertEquals("BORRADOR", registrado.historial!!.single().estadoAnterior)
    }

    @Test
    fun `cerrar una visita local la marca como cerrada`() {
        val v = VisitaDto(-1, "OT-1", 4, "2026-11-18", "SOLO_PRUEBAS", 2, listOf(3), null, false, null)
        val cerrada = v.cerradaLocal("2026-11-18T12:00:00")
        assertTrue(cerrada.cerrada)
        assertEquals("2026-11-18T12:00:00", cerrada.fechaHoraCierre)
    }

    @Test
    fun `las condiciones ambientales se guardan en la copia`() {
        val p = base().conCondiciones(22.5, 60.0)
        assertEquals(22.5, p.temperaturaC!!, 0.0)
        assertEquals(60.0, p.humedadPct!!, 0.0)
    }
}
