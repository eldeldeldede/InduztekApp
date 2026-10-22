package cl.induztek.app.model

import java.time.LocalDate

data class InstalacionesUiState(
    val filtro: String = "",
    val instalaciones: List<Instalacion> = emptyList()
)

data class EquiposUiState(
    val instalacion: Instalacion? = null,
    val filtro: String = "",
    val equipos: List<Equipo> = emptyList()
)

data class ComparacionValor(
    val serie: SerieMedicion,
    val actual: PuntoHistorial,
    val anterior: PuntoHistorial?,
    val variacionPct: Double?,
    val significativa: Boolean
)

data class HistorialEquipoUiState(
    val equipo: Equipo? = null,
    val protocolos: List<ResumenProtocolo> = emptyList(),
    val comparaciones: List<ComparacionValor> = emptyList(),
    val hoy: LocalDate = LocalDate.now()
)