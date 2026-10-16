package cl.induztek.app.model

data class Usuario(
    val idUsuario: Long = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val password_hash: String,
    val activo: Boolean,
    val giro: String,
    val contactoNombre: String,
    val contactoCargo: String,
    val idRol: Long
)