package cl.induztek.app.model

import java.time.LocalDate

// =====================================================================
//  ListasUiState.kt — el estado de las pantallas de LISTA y de detalle
//  (un data class por pantalla, igual que FormularioUiState / PerfilUiState en MiRegistro)
// =====================================================================

// Pantalla de instalaciones: el cliente ve solo las suyas (RN-18), el personal ve todas.
data class InstalacionesUiState(
    val filtro: String = "",
    val instalaciones: List<Instalacion> = emptyList()
)

// Pantalla de equipos de UNA instalación
data class EquiposUiState(
    val instalacion: Instalacion? = null,
    val filtro: String = "",
    val equipos: List<Equipo> = emptyList()
)

// Comparación de UN valor medido contra la prueba anterior (RN-09, HU-06)
data class ComparacionValor(
    val serie: SerieMedicion,
    val actual: PuntoHistorial,
    val anterior: PuntoHistorial?,       // null si es la primera vez que se mide
    val variacionPct: Double?,           // null si no hay anterior
    val significativa: Boolean           // |variación| > 25 %
)

// Pantalla de historial de UN equipo (HU-11, HU-12)
data class HistorialEquipoUiState(
    val equipo: Equipo? = null,
    val protocolos: List<ResumenProtocolo> = emptyList(),
    val comparaciones: List<ComparacionValor> = emptyList(),
    val hoy: LocalDate = LocalDate.now()   // para marcar "Atrasado" (RN-16)
)
