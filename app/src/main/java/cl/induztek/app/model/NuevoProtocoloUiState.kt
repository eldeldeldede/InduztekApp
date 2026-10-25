package cl.induztek.app.model

data class NuevoProtocoloUiState(
    val equipo: Equipo? = null,
    val ingenieros: List<Usuario> = emptyList(),

    val numeroOt: String = "",
    val tipoVisita: TipoVisita = TipoVisita.SOLO_PRUEBAS,
    val idIngeniero: Long? = null,

    val errorOt: String? = null,
    val errorIngeniero: String? = null,
    val errorGeneral: String? = null,

    val idProtocoloCreado: Long? = null
) {
    val nombreIngeniero: String
        get() = ingenieros.firstOrNull { it.idUsuario == idIngeniero }?.nombre ?: ""
}
