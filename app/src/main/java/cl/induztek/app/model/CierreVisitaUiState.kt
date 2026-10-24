package cl.induztek.app.model

data class ActaProtocolo(
    val numero: String,
    val tagEquipo: String,
    val resultado: TipoResultado?,
    val lineas: List<String>,
    val estado: TipoEstadoProtocolo? = null
)

data class ProtocoloPendiente(
    val idProtocolo: Long,
    val numero: String,
    val tagEquipo: String,
    val estado: TipoEstadoProtocolo,
    val pruebasFaltantes: List<String>
)

data class CierreVisitaUiState(
    val cargado: Boolean = false,
    val visita: Visita? = null,
    val instalacion: Instalacion? = null,
    val ingenieroNombre: String = "",
    val participantes: List<String> = emptyList(),
    val protocolos: List<ResumenProtocolo> = emptyList(),
    val acta: List<ActaProtocolo> = emptyList(),
    val pendientes: List<ProtocoloPendiente> = emptyList(),
    val recepcion: RecepcionActa? = null,
    val soloConsulta: Boolean = false,

    val receptorNombre: String = "",
    val receptorRut: String = "",
    val receptorCargo: String = "",
    val errorNombre: String? = null,
    val errorRut: String? = null,
    val errorCargo: String? = null,

    val mensaje: String? = null,
    val errorGeneral: String? = null
) {
    val estaCerrada: Boolean get() = visita?.estaCerrada == true

    val resultadoGlobal: TipoResultado?
        get() = acta.mapNotNull { it.resultado }.maxOrNull()
}
