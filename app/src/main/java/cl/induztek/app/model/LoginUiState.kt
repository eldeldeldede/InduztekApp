package cl.induztek.app.model

// =====================================================================
//  LoginUiState.kt — TODO lo que la pantalla de login necesita para dibujarse
//  (mismo patrón que FormularioUiState de MiRegistro: un data class inmutable;
//  el ViewModel crea una copia nueva con .copy() cada vez que algo cambia).
// =====================================================================
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,

    // Un error por campo (null = sin error), como errorNombre / errorCorreo de MiRegistro
    val errorCorreo: String? = null,
    val errorClave: String? = null,

    // Error que no es de un campo: "Correo o contraseña incorrectos", "Cuenta desactivada"
    val errorGeneral: String? = null
)
