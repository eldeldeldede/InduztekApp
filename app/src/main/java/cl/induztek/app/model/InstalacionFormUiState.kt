package cl.induztek.app.model

// Formulario para crear o editar una instalación (planta/sitio de un cliente).
data class InstalacionFormUiState(
    val idInstalacion: Long = 0,                  // 0 = nueva
    val clientes: List<Usuario> = emptyList(),
    val comunas: List<Comuna> = emptyList(),
    val cliente: Usuario? = null,
    val clienteNombreFijo: String? = null,         // al editar el cliente no se cambia
    val nombre: String = "",
    val ubicacion: String = "",
    val comuna: Comuna? = null,
    val observaciones: String = "",
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val errorCliente: String? = null,
    val errorNombre: String? = null,
    val errorComuna: String? = null,
    val errorGeneral: String? = null,
    val guardado: Boolean = false
) {
    val esNueva: Boolean get() = idInstalacion == 0L
}
