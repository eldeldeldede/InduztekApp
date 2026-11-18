package cl.induztek.app.model

import cl.induztek.app.network.RolDto

// Estado de la pantalla "Crear usuario" (solo Jefe técnico).
// El Jefe elige el rol pero NO la contraseña: el servidor genera una temporal y la envía al correo.
data class CrearUsuarioUiState(
    val rut: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rol: RolDto? = null,
    // Roles para la lista desplegable. Parte con los 4 roles del caso y se reemplaza por los del servidor.
    val roles: List<RolDto> = CodigoRol.entries.map { RolDto(it.id, it.codigo, it.etiqueta) },
    // Solo si el rol es CLIENTE
    val giro: String = "",
    val contactoNombre: String = "",
    val contactoCargo: String = "",

    val cargando: Boolean = false,
    val errorRut: String? = null,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorRol: String? = null,
    val errorGeneral: String? = null,
    // Mensaje de éxito: "Usuario creado. Contraseña temporal enviada a ..."
    val exito: String? = null
) {
    val esCliente: Boolean get() = rol?.codigo == CodigoRol.CLIENTE.codigo
}
