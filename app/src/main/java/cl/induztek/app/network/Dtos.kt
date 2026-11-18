package cl.induztek.app.network

import androidx.annotation.Keep
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.Usuario

// =====================================================================
//  Dtos.kt — "Data Transfer Objects": la forma del JSON que viaja entre la app y el servidor
//  ---------------------------------------------------------------------
//  Los nombres de las propiedades DEBEN ser iguales a los del JSON del microservicio
//  (UsuarioResponse, CrearUsuarioRequest...). @Keep evita que el modo release renombre
//  las propiedades y se rompa la lectura del JSON.
// =====================================================================

@Keep
data class LoginSolicitud(val correo: String, val contrasena: String)

@Keep
data class CrearUsuarioSolicitud(
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val rolId: Long,
    val giro: String?,
    val contactoNombre: String?,
    val contactoCargo: String?
)

@Keep
data class CambiarContrasenaSolicitud(val actual: String, val nueva: String)

@Keep
data class RolDto(val id: Long, val codigo: String, val nombre: String)

@Keep
data class UsuarioDto(
    val id: Long,
    val rolId: Long,
    val rolCodigo: String,
    val rolNombre: String?,
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val activo: Boolean,
    val giro: String?,
    val contactoNombre: String?,
    val contactoCargo: String?,
    val debeCambiarContrasena: Boolean
) {
    // Convierte la respuesta del servidor al `Usuario` que usa el resto de la app.
    // La clave NUNCA viaja de vuelta: passwordHash queda vacío.
    fun aUsuario(): Usuario = Usuario(
        idUsuario = id,
        idRol = rolId,
        codigoRol = CodigoRol.desdeCodigo(rolCodigo),
        rut = rut,
        nombre = nombre,
        correo = correo,
        telefono = telefono,
        passwordHash = "",
        activo = activo,
        giro = giro,
        contactoNombre = contactoNombre,
        contactoCargo = contactoCargo,
        debeCambiarContrasena = debeCambiarContrasena
    )
}
