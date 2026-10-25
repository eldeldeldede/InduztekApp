package cl.induztek.app.model

data class Usuario(
    val idUsuario: Long = 0,
    val idRol: Long,
    val codigoRol: CodigoRol,
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String? = null,
    val passwordHash: String,
    val activo: Boolean = true,
    val giro: String? = null,
    val contactoNombre: String? = null,
    val contactoCargo: String? = null
) {

    val esCliente: Boolean get() = codigoRol == CodigoRol.CLIENTE
}
