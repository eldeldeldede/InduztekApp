package cl.induztek.app.local

import androidx.annotation.Keep
import cl.induztek.app.network.ProtocoloActualizacionSolicitud
import cl.induztek.app.network.ProtocoloSolicitud
import cl.induztek.app.network.PruebaSolicitud
import cl.induztek.app.network.VisitaSolicitud

// =====================================================================
//  OperacionPendiente.kt — un cambio hecho SIN conexión que falta enviar al servidor
//  ---------------------------------------------------------------------
//  Cuando no hay red, la app guarda el cambio en el teléfono (cola) y lo envía en el mismo orden
//  cuando vuelve la conexión. Cada operación lleva "referencias" a la visita, protocolo o prueba que
//  toca. Si ese elemento se creó sin conexión todavía no tiene id real: se usa un id LOCAL NEGATIVO
//  y, al sincronizar, se cambia por el id que entregue el servidor.
// =====================================================================
enum class TipoOperacion {
    CREAR_VISITA,
    CREAR_PROTOCOLO,
    ACTUALIZAR_CONDICIONES,
    REGISTRAR_PRUEBA,
    ELIMINAR_PRUEBA,
    REGISTRAR_TERRENO,
    CERRAR_VISITA
}

enum class EstadoOperacion { PENDIENTE, FALLIDA }

@Keep
data class OperacionPendiente(
    val id: Long,
    val tipo: TipoOperacion,
    val actorId: Long,
    val descripcion: String,
    val refVisita: Long? = null,
    val refProtocolo: Long? = null,
    val refPrueba: Long? = null,
    val visita: VisitaSolicitud? = null,
    val protocolo: ProtocoloSolicitud? = null,
    val condiciones: ProtocoloActualizacionSolicitud? = null,
    val prueba: PruebaSolicitud? = null,
    val estado: EstadoOperacion = EstadoOperacion.PENDIENTE,
    val error: String? = null
)
