package cl.induztek.app.local

import cl.induztek.app.network.ProtocoloDto
import cl.induztek.app.network.VisitaDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AlmacenLocalTest {

    private lateinit var carpeta: File

    @Before
    fun preparar() {
        carpeta = Files.createTempDirectory("induztek-local").toFile()
        AlmacenLocal.iniciar(carpeta)
        AlmacenLocal.usuarioId = 1
    }

    private fun operacion(tipo: TipoOperacion, refProtocolo: Long? = null, refVisita: Long? = null, refPrueba: Long? = null) =
        OperacionPendiente(0, tipo, 3, "op", refVisita = refVisita, refProtocolo = refProtocolo, refPrueba = refPrueba)

    private fun protocolo(id: Long) = ProtocoloDto(
        id, "PEND-${-id}", -1, "OT-1", 10, 3, "2026-11-18T10:00:00", null, null, null, null,
        "BORRADOR", null, null, false, emptyList(), emptyList()
    )

    @Test
    fun `los ids locales son negativos y distintos`() {
        val a = AlmacenLocal.nuevoIdLocal()
        val b = AlmacenLocal.nuevoIdLocal()
        assertTrue(a < 0 && b < 0)
        assertTrue(a != b)
        assertTrue(AlmacenLocal.esLocal(a))
    }

    @Test
    fun `un id real pasa tal cual y uno local solo se resuelve si hay equivalencia`() {
        val local = AlmacenLocal.nuevoIdLocal()
        assertEquals(15L, AlmacenLocal.idReal(15))
        assertNull(AlmacenLocal.idReal(local))
        AlmacenLocal.registrarEquivalencia(local, 99)
        assertEquals(99L, AlmacenLocal.idReal(local))
    }

    @Test
    fun `la cola conserva el orden y asigna numeros crecientes`() {
        val primera = AlmacenLocal.encolar(operacion(TipoOperacion.CREAR_VISITA))
        val segunda = AlmacenLocal.encolar(operacion(TipoOperacion.CREAR_PROTOCOLO))
        assertTrue(segunda.id > primera.id)
        assertEquals(listOf(primera.id, segunda.id), AlmacenLocal.operaciones().map { it.id })
        AlmacenLocal.quitarOperacion(primera.id)
        assertEquals(listOf(segunda.id), AlmacenLocal.operaciones().map { it.id })
    }

    @Test
    fun `una prueba registrada y borrada sin conexion sale de la cola`() {
        AlmacenLocal.encolar(operacion(TipoOperacion.REGISTRAR_PRUEBA, refProtocolo = -1, refPrueba = -5))
        assertTrue(AlmacenLocal.quitarRegistroDePrueba(-5))
        assertTrue(AlmacenLocal.operaciones().isEmpty())
        assertFalse(AlmacenLocal.quitarRegistroDePrueba(-5))
    }

    @Test
    fun `la cola y las copias locales sobreviven a reiniciar la app`() {
        AlmacenLocal.encolar(operacion(TipoOperacion.CREAR_PROTOCOLO, refProtocolo = -1))
        AlmacenLocal.guardarProtocoloLocal(protocolo(-1))
        AlmacenLocal.iniciar(carpeta) // simula abrir la app otra vez
        assertEquals(1, AlmacenLocal.operaciones().size)
        assertEquals("PEND-1", AlmacenLocal.protocoloLocal(-1)?.numero)
    }

    @Test
    fun `las copias sin operaciones esperando se limpian`() {
        AlmacenLocal.guardarProtocoloLocal(protocolo(-1))
        AlmacenLocal.guardarProtocoloLocal(protocolo(-2))
        AlmacenLocal.encolar(operacion(TipoOperacion.REGISTRAR_TERRENO, refProtocolo = -2))
        AlmacenLocal.limpiarCopiasSinOperaciones()
        assertNull(AlmacenLocal.protocoloLocal(-1))
        assertEquals(-2L, AlmacenLocal.protocoloLocal(-2)?.id)
    }

    @Test
    fun `la cache se separa por usuario`() {
        AlmacenLocal.guardarCache("equipos_1", "[1]")
        assertEquals("[1]", AlmacenLocal.leerCache("equipos_1"))
        AlmacenLocal.usuarioId = 2
        assertNull(AlmacenLocal.leerCache("equipos_1"))
    }

    @Test
    fun `una visita local se guarda y se recupera`() {
        val v = VisitaDto(-1, "OT-9", 4, "2026-11-18", "SOLO_PRUEBAS", 2, listOf(3), null, false, null)
        AlmacenLocal.guardarVisitaLocal(v)
        assertEquals("OT-9", AlmacenLocal.visitaLocal(-1)?.numeroOt)
        assertEquals(1, AlmacenLocal.visitasLocales().size)
    }
}
