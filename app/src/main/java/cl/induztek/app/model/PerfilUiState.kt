package cl.induztek.app.model

// Estado de "Mi perfil". El RUT y el correo se muestran pero NO se pueden editar.
data class PerfilUiState(
    val rut: String = "",
    val correo: String = "",
    val rolEtiqueta: String = "",
    val esCliente: Boolean = false,
    val nombre: String = "",
    val telefono: String = "",
    val giro: String = "",
    val contactoNombre: String = "",
    val contactoCargo: String = "",
    val cargado: Boolean = false,
    val guardando: Boolean = false,
    val errorNombre: String? = null,
    val errorTelefono: String? = null,
    val errorGeneral: String? = null,
    val exito: String? = null
)
