package cl.induztek.app.model

data class NuevoProtocoloUiState(
    val equipo: Equipo? = null,
    val ingenieros: List<Usuario> = emptyList(),
    val tecnicos: List<Usuario> = emptyList(),
    val idActor: Long? = null,
    val idsOtrosParticipantes: Set<Long> = emptySet(),

    val numeroOt: String = "",
    val tipoVisita: TipoVisita = TipoVisita.SOLO_PRUEBAS,
    val idIngeniero: Long? = null,

    val errorOt: String? = null,
    val errorIngeniero: String? = null,
    val errorGeneral: String? = null,

    val idProtocoloCreado: Long? = null
) {
    val candidatosParticipantes: List<Usuario>
        get() = (tecnicos + ingenieros).filter { it.idUsuario != idActor }.sortedBy { it.nombre }

    val nombreIngeniero: String
        get() = ingenieros.firstOrNull { it.idUsuario == idIngeniero }?.nombre ?: ""
}
