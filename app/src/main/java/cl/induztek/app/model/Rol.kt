package cl.induztek.app.model

// Una fila de la tabla "rol". (CORREGIDO: id_rol -> idRol, para usar camelCase en todo el código)
data class Rol(
    val idRol: Long,
    val codigo: CodigoRol,
    val nombre: String
)
