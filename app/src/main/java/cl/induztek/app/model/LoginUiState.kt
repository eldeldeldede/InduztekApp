package cl.induztek.app.model

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,

    val errorCorreo: String? = null,
    val errorClave: String? = null,

    val errorGeneral: String? = null
)
