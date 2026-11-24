package cl.induztek.app.model

data class EstadoActa(
    val pendiente: Boolean = false,
    val formalizada: Boolean = false,
    val resultado: TipoResultado? = null
)

data class ActasUiState(
    val cargado: Boolean = false,
    val instalacion: Instalacion? = null,
    val visitas: List<Visita> = emptyList(),
    val esCliente: Boolean = false,
    val estados: Map<Long, EstadoActa> = emptyMap(),
    val cargando: Boolean = false,
    val error: String? = null
)
