package cl.induztek.app.repository

import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ActualizarPerfilSolicitud
import cl.induztek.app.network.ApiClient
import cl.induztek.app.network.CambiarContrasenaSolicitud
import cl.induztek.app.network.CambiarEstadoSolicitud
import cl.induztek.app.network.ConfirmarRecuperacionSolicitud
import cl.induztek.app.network.CrearUsuarioSolicitud
import cl.induztek.app.network.LoginSolicitud
import cl.induztek.app.network.RecuperacionSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.RolDto
import cl.induztek.app.network.UsuarioDto
import cl.induztek.app.network.llamarApi
import cl.induztek.app.network.llamarConCache
import cl.induztek.app.network.mapear

// =====================================================================
//  UsuarioRepositorio.kt — la ÚNICA puerta de la app hacia usuarios-service
//  ---------------------------------------------------------------------
//  Los ViewModels no conocen Retrofit: piden aquí lo que necesitan y reciben un ResultadoRed
//  (Exito / ErrorServidor / SinConexion). Si mañana cambia la fuente de los datos (caché, otro
//  servidor), solo se toca esta clase.
// =====================================================================
class UsuarioRepositorio {

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoRed<Usuario> =
        llamarApi { ApiClient.usuarios.login(LoginSolicitud(correo, clave)) }.mapear { it.aUsuario() }

    suspend fun roles(): ResultadoRed<List<RolDto>> = llamarApi { ApiClient.usuarios.roles() }

    suspend fun crear(idActor: Long, solicitud: CrearUsuarioSolicitud): ResultadoRed<Usuario> =
        llamarApi { ApiClient.usuarios.crear(idActor, solicitud) }.mapear { it.aUsuario() }

    suspend fun cambiarContrasena(idUsuario: Long, actual: String, nueva: String): ResultadoRed<Unit> =
        llamarApi { ApiClient.usuarios.cambiarContrasena(idUsuario, CambiarContrasenaSolicitud(actual, nueva)) }

    suspend fun obtener(idUsuario: Long): ResultadoRed<Usuario> =
        llamarApi { ApiClient.usuarios.obtener(idUsuario) }.mapear { it.aUsuario() }

    suspend fun listarActivosPorRol(rol: CodigoRol): ResultadoRed<List<Usuario>> =
        llamarConCache<List<UsuarioDto>>("usuarios_rol_${rol.id}") { ApiClient.usuarios.listar(rol.id) }
            .mapear { lista -> lista.filter { it.activo }.map { it.aUsuario() } }

    suspend fun listarClientes(): ResultadoRed<List<Usuario>> =
        llamarApi { ApiClient.usuarios.listar(CodigoRol.CLIENTE.id) }.mapear { lista -> lista.map { it.aUsuario() } }

    // id -> nombre de TODOS los usuarios; sirve para mostrar "técnico", "ingeniero" o "empresa" por nombre.
    suspend fun nombres(): Map<Long, String> =
        when (val r = llamarConCache<List<UsuarioDto>>("usuarios_todos") { ApiClient.usuarios.listar(null) }) {
            is ResultadoRed.Exito -> r.datos.associate { it.id to it.nombre }
            else -> emptyMap()
        }

    suspend fun actualizarPerfil(
        idActor: Long,
        idUsuario: Long,
        solicitud: ActualizarPerfilSolicitud
    ): ResultadoRed<Usuario> =
        llamarApi { ApiClient.usuarios.actualizarPerfil(idActor, idUsuario, solicitud) }.mapear { it.aUsuario() }

    // Desactivar/activar una cuenta: solo lo permite el Jefe técnico (el servidor lo valida).
    suspend fun cambiarEstado(idActor: Long, idUsuario: Long, activo: Boolean): ResultadoRed<Usuario> =
        llamarApi { ApiClient.usuarios.cambiarEstado(idActor, idUsuario, CambiarEstadoSolicitud(activo)) }
            .mapear { it.aUsuario() }

    // El servidor manda el código por correo; aquí solo se pide que lo envíe.
    suspend fun solicitarRecuperacion(correo: String): ResultadoRed<String> =
        llamarApi { ApiClient.usuarios.solicitarRecuperacion(RecuperacionSolicitud(correo)) }
            .mapear { it.mensaje ?: "Enviamos un código de recuperación a tu correo" }

    suspend fun confirmarRecuperacion(correo: String, codigo: String, nueva: String): ResultadoRed<Unit> =
        llamarApi { ApiClient.usuarios.confirmarRecuperacion(ConfirmarRecuperacionSolicitud(correo, codigo, nueva)) }
}
