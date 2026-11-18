package cl.induztek.app.local

import androidx.annotation.Keep
import cl.induztek.app.network.ProtocoloDto
import cl.induztek.app.network.VisitaDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

// =====================================================================
//  AlmacenLocal.kt — lo único que la app guarda en el teléfono para trabajar sin conexión
//  ---------------------------------------------------------------------
//  Ya NO hay base de datos local con los datos del negocio: la fuente de verdad es el servidor.
//  Aquí solo hay tres cosas, todas en archivos JSON:
//    1) COLA de cambios hechos sin conexión (se envían al volver la red).
//    2) COPIAS locales de las visitas/protocolos que se crearon o tocaron sin conexión, para que
//       la pantalla los muestre mientras no se envían.
//    3) CACHÉ de lo último que se vio con conexión (lista de equipos, último protocolo de cada
//       equipo, catálogos), para poder consultarlo en terreno. Separada por usuario.
//  Recibe la carpeta por parámetro, así también se puede probar en el computador sin Android.
// =====================================================================
@Keep
data class EstadoLocal(
    val cola: MutableList<OperacionPendiente> = mutableListOf(),
    val protocolos: MutableMap<Long, ProtocoloDto> = mutableMapOf(),
    val visitas: MutableMap<Long, VisitaDto> = mutableMapOf(),
    val equivalencias: MutableMap<Long, Long> = mutableMapOf(),
    var contadorLocal: Long = 0,
    var contadorOperacion: Long = 0
)

object AlmacenLocal {

    private val gson = Gson()
    private val candado = Any()
    private var carpeta: File? = null
    private var estado = EstadoLocal()

    // Quién tiene la sesión: la caché se separa por usuario para que nadie vea datos de otro (p. ej. otro cliente).
    @Volatile
    var usuarioId: Long = 0

    private val _cola = MutableStateFlow<List<OperacionPendiente>>(emptyList())
    val cola: StateFlow<List<OperacionPendiente>> = _cola.asStateFlow()

    fun iniciar(directorio: File) {
        synchronized(candado) {
            carpeta = directorio.also { File(it, "cache").mkdirs() }
            val archivo = File(directorio, "estado.json")
            estado = try {
                if (archivo.exists()) gson.fromJson(archivo.readText(), EstadoLocal::class.java) ?: EstadoLocal()
                else EstadoLocal()
            } catch (e: Exception) {
                EstadoLocal()
            }
            _cola.value = estado.cola.toList()
        }
    }

    private fun guardar() {
        val dir = carpeta ?: return
        File(dir, "estado.json").writeText(gson.toJson(estado))
        _cola.value = estado.cola.toList()
    }

    // ---------- Ids locales (negativos, para no chocar nunca con los del servidor) ----------

    fun nuevoIdLocal(): Long = synchronized(candado) {
        estado.contadorLocal += 1
        guardar()
        -estado.contadorLocal
    }

    fun esLocal(id: Long): Boolean = id < 0

    // Id real de algo creado sin conexión (null si todavía no se sincroniza). Los ids reales pasan tal cual.
    fun idReal(ref: Long): Long? = synchronized(candado) { if (ref > 0) ref else estado.equivalencias[ref] }

    fun registrarEquivalencia(local: Long, real: Long) = synchronized(candado) {
        estado.equivalencias[local] = real
        guardar()
    }

    // ---------- Cola de operaciones ----------

    fun encolar(operacion: OperacionPendiente): OperacionPendiente = synchronized(candado) {
        estado.contadorOperacion += 1
        val con = operacion.copy(id = estado.contadorOperacion)
        estado.cola.add(con)
        guardar()
        con
    }

    fun actualizarOperacion(operacion: OperacionPendiente) = synchronized(candado) {
        val i = estado.cola.indexOfFirst { it.id == operacion.id }
        if (i >= 0) {
            estado.cola[i] = operacion
            guardar()
        }
    }

    fun quitarOperacion(id: Long) = synchronized(candado) {
        estado.cola.removeAll { it.id == id }
        guardar()
    }

    // Si una prueba se registró y se borró SIN conexión, nunca llega al servidor: se retira de la cola.
    fun quitarRegistroDePrueba(refPrueba: Long): Boolean = synchronized(candado) {
        val quitada = estado.cola.removeAll { it.tipo == TipoOperacion.REGISTRAR_PRUEBA && it.refPrueba == refPrueba }
        if (quitada) guardar()
        quitada
    }

    fun operaciones(): List<OperacionPendiente> = synchronized(candado) { estado.cola.toList() }

    fun hayOperacionesPendientes(): Boolean = synchronized(candado) {
        estado.cola.any { it.estado == EstadoOperacion.PENDIENTE }
    }

    // ---------- Copias locales de lo que se creó o cambió sin conexión ----------

    fun protocoloLocal(id: Long): ProtocoloDto? = synchronized(candado) { estado.protocolos[id] }
    fun protocolosLocales(): List<ProtocoloDto> = synchronized(candado) { estado.protocolos.values.toList() }
    fun guardarProtocoloLocal(protocolo: ProtocoloDto) = synchronized(candado) {
        estado.protocolos[protocolo.id] = protocolo
        guardar()
    }

    fun visitaLocal(id: Long): VisitaDto? = synchronized(candado) { estado.visitas[id] }
    fun visitasLocales(): List<VisitaDto> = synchronized(candado) { estado.visitas.values.toList() }
    fun guardarVisitaLocal(visita: VisitaDto) = synchronized(candado) {
        estado.visitas[visita.id] = visita
        guardar()
    }

    // Después de sincronizar, se borran las copias locales que ya no tienen operaciones esperando.
    fun limpiarCopiasSinOperaciones() = synchronized(candado) {
        val protocolosEnCola = estado.cola.mapNotNull { it.refProtocolo }.toSet()
        val visitasEnCola = estado.cola.mapNotNull { it.refVisita }.toSet()
        estado.protocolos.keys.retainAll(protocolosEnCola)
        estado.visitas.keys.retainAll(visitasEnCola)
        if (estado.cola.isEmpty()) estado.equivalencias.clear()
        guardar()
    }

    // ---------- Caché de lo último visto con conexión (por usuario) ----------

    private fun archivoCache(clave: String): File? {
        val dir = carpeta ?: return null
        val seguro = clave.replace(Regex("[^A-Za-z0-9_.-]"), "_")
        return File(File(dir, "cache"), "u${usuarioId}_$seguro.json")
    }

    fun guardarCache(clave: String, json: String) {
        try {
            archivoCache(clave)?.writeText(json)
        } catch (e: Exception) {
            // La caché es solo una ayuda: si falla, la app sigue funcionando.
        }
    }

    fun leerCache(clave: String): String? = try {
        archivoCache(clave)?.takeIf { it.exists() }?.readText()
    } catch (e: Exception) {
        null
    }
}
