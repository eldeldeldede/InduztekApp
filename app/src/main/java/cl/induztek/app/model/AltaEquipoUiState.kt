package cl.induztek.app.model

// =====================================================================
//  AltaEquipoUiState.kt — el formulario de alta de equipo (HU-01), con sus campos y errores.
//  Los números llegan como TEXTO porque eso es lo que escribe el usuario; el ViewModel
//  los convierte con Unidades.parsearDecimal() recién al guardar.
// =====================================================================

// Ids de la tabla tipo_equipo (ver DatosIniciales.kt). Sirven para decidir qué campos de placa
// se muestran: un transformador tiene potencia y dos tensiones; un interruptor, corriente y contactos.
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
    val fotoPlaca: String? = null,   // ruta de la foto tomada con la cámara

    val errorFoto: String? = null,
    val errorTag: String? = null,
    val errorAnio: String? = null,
    val errorPotencia: String? = null,
    val errorTensionPrimaria: String? = null,
    val errorTensionSecundaria: String? = null,
    val errorCorriente: String? = null,
    val errorMaxContactos: String? = null,

    // HU-01: si el TAG ya existe en la instalación se ofrece usar el equipo registrado
    val idEquipoExistente: Long? = null,
    val errorGeneral: String? = null,
    val guardadoExitoso: Boolean = false
) {
    // Qué campos de placa mostrar según el tipo elegido
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
