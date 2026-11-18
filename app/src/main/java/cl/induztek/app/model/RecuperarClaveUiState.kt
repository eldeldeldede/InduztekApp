package cl.induztek.app.model

// Los dos pasos de "Olvidé mi contraseña": primero se pide el código al correo, luego se elige la nueva clave.
enum class PasoRecuperacion { SOLICITAR, CONFIRMAR }

data class RecuperarClaveUiState(
    val paso: PasoRecuperacion = PasoRecuperacion.SOLICITAR,
    val correo: String = "",
    val codigo: String = "",
    val nueva: String = "",
    val confirmacion: String = "",
    val clavesVisibles: Boolean = false,
    val cargando: Boolean = false,
    val errorCorreo: String? = null,
    val errorCodigo: String? = null,
    val errorNueva: String? = null,
    val errorConfirmacion: String? = null,
    val errorGeneral: String? = null,
    // Texto del servidor: "Enviamos un código a tu correo..."
    val informacion: String? = null
)
