package cl.induztek.app.local

import org.junit.Assert.assertEquals
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

    private fun operacion(tipo: TipoOperacion) = OperacionPendiente(0, tipo, 3, "op")

    @Test
    fun `los ids locales son negativos y distintos`() {
        val a = AlmacenLocal.nuevoIdLocal()
        val b = AlmacenLocal.nuevoIdLocal()
        assertTrue(a < 0 && b < 0)
        assertTrue(a != b)
        assertTrue(AlmacenLocal.esLocal(a))
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
}
