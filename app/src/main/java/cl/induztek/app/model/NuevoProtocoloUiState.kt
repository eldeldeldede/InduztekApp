package cl.induztek.app.model

// =====================================================================
//  NuevoProtocoloUiState.kt — formulario para iniciar un protocolo (HU-02):
//  se indica la visita (orden de trabajo, tipo e ingeniero a cargo) y se crea el
//  protocolo en estado Borrador. Si la OT ya existe se reutiliza la visita (varios equipos
//  en una misma visita comparten acta, Aclaración 5).
// =====================================================================
data class NuevoProtocoloUiState(
    val equipo: Equipo? = null,
    val ingenieros: List<Usuario> = emptyList(),

    val numeroOt: String = "",
    val tipoVisita: TipoVisita = TipoVisita.SOLO_PRUEBAS,
    val idIngeniero: Long? = null,

    val errorOt: String? = null,
    val errorIngeniero: String? = null,
    val errorGeneral: String? = null,

    // Al guardar: id del protocolo creado (o ya existente) al que hay que navegar
    val idProtocoloCreado: Long? = null
) {
    val nombreIngeniero: String
        get() = ingenieros.firstOrNull { it.idUsuario == idIngeniero }?.nombre ?: ""
}
