package cl.induztek.app.model

// Estado de la pantalla "Cambiar contraseña" (primer ingreso con la clave temporal).
data class CambiarClaveUiState(
    val actual: String = "",
    val nueva: String = "",
    val confirmacion: String = "",
    val clavesVisibles: Boolean = false,
    val cargando: Boolean = false,
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorConfirmacion: String? = null,
    val errorGeneral: String? = null
)
