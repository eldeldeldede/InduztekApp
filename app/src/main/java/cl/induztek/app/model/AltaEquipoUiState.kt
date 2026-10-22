package cl.induztek.app.model

object TiposEquipoIds {
    const val TRANSFORMADOR = 1L
    const val INTERRUPTOR = 2L
    const val CELDA_MT = 3L
    const val CABLE = 4L
    const val RELE = 5L
}

data class AltaEquipoUiState(
    val tipos: List<TipoEquipo> = emptyList(),
    val idTipoEquipo: Long = TiposEquipoIds.TRANSFORMADOR,

    val tag: String = "",
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val anioFabricacion: String = "",
    val potenciaKva: String = "",
    val tensionPrimariaKv: String = "",
    val tensionSecundariaKv: String = "",
    val corrienteNominalA: String = "",
    val maxContactosUohm: String = "",
    val observaciones: String = "",

    val errorTag: String? = null,
    val errorAnio: String? = null,
    val errorPotencia: String? = null,
    val errorTensionPrimaria: String? = null,
    val errorTensionSecundaria: String? = null,
    val errorCorriente: String? = null,
    val errorMaxContactos: String? = null,

    val idEquipoExistente: Long? = null,
    val errorGeneral: String? = null,
    val guardadoExitoso: Boolean = false
) {
    val mostrarPotencia get() = idTipoEquipo == TiposEquipoIds.TRANSFORMADOR
    val mostrarTensionPrimaria get() = idTipoEquipo != TiposEquipoIds.RELE
    val mostrarTensionSecundaria get() = idTipoEquipo == TiposEquipoIds.TRANSFORMADOR
    val mostrarCorriente get() = idTipoEquipo == TiposEquipoIds.INTERRUPTOR ||
            idTipoEquipo == TiposEquipoIds.CELDA_MT
    val mostrarMaxContactos get() = idTipoEquipo == TiposEquipoIds.INTERRUPTOR ||
            idTipoEquipo == TiposEquipoIds.CELDA_MT

    val nombreTipoSeleccionado: String
        get() = tipos.firstOrNull { it.idTipoEquipo == idTipoEquipo }?.nombre ?: ""
}