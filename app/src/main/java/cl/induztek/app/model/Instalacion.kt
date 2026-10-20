package cl.induztek.app.model

data class Instalacion(
    val idInstalacion: Long,
    val idUsuario: Long, // foreign key con id del usuario tipo Cliente
    val nombre: String,
    val ubicacion: String,
    val idComuna: Long,
    val observacionesAcceso: String? = null,
    val comunaNombre: String = "",
    val clienteNombre: String = ""
)