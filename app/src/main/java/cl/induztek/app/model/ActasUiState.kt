package cl.induztek.app.model

data class ActasUiState(
    val cargado: Boolean = false,
    val instalacion: Instalacion? = null,
    val visitas: List<Visita> = emptyList(),
    val esCliente: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null
)
