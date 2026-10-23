package cl.induztek.app.model

data class Instalacion(
    val idInstalacion: Long,
    val idUsuario: Long, // foreign key con id del usuario tipo Cliente (columna id_usuario_cliente)
    val nombre: String,
    val ubicacion: String,
    val idComuna: Long,
    val observacionesAcceso: String? = null,
    // CORREGIDO: los dos siguientes NO son columnas de "instalacion": llegan por JOIN
    // (comuna y usuario). Sirven para mostrar la lista sin hacer más consultas.
    val comunaNombre: String = "",
    val clienteNombre: String = ""
)
