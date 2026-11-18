package cl.induztek.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// =====================================================================
//  UsuariosApi.kt — los endpoints de usuarios-service (puerto 8081)
//  ---------------------------------------------------------------------
//  Cada función es una llamada HTTP. `suspend` = se ejecuta en segundo plano sin congelar la
//  pantalla (se llama desde viewModelScope.launch). Si el servidor responde con error (400, 403...),
//  Retrofit lanza HttpException; si no hay red, IOException (ver llamarApi en ResultadoRed.kt).
//
//  "X-Usuario-Id" es la cabecera con la que el servicio sabe QUIÉN hace la acción
//  (así valida que solo el Jefe técnico cree usuarios).
// =====================================================================
interface UsuariosApi {

    @POST("api/usuarios/login")
    suspend fun login(@Body solicitud: LoginSolicitud): UsuarioDto

    @POST("api/usuarios")
    suspend fun crear(
        @Header("X-Usuario-Id") idActor: Long,
        @Body solicitud: CrearUsuarioSolicitud
    ): UsuarioDto

    // Responde 204 (sin cuerpo): por eso devuelve Unit
    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(
        @Path("id") idUsuario: Long,
        @Body solicitud: CambiarContrasenaSolicitud
    )

    @GET("api/roles")
    suspend fun roles(): List<RolDto>

    @GET("api/usuarios")
    suspend fun listar(@Query("rolId") rolId: Long?): List<UsuarioDto>

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") idUsuario: Long): UsuarioDto

    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") idUsuario: Long,
        @Body solicitud: ActualizarPerfilSolicitud
    ): UsuarioDto

    @PUT("api/usuarios/{id}/estado")
    suspend fun cambiarEstado(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") idUsuario: Long,
        @Body solicitud: CambiarEstadoSolicitud
    ): UsuarioDto

    @POST("api/usuarios/recuperacion")
    suspend fun solicitarRecuperacion(@Body solicitud: RecuperacionSolicitud): RecuperacionDto

    // Responde 204 (sin cuerpo)
    @POST("api/usuarios/recuperacion/confirmar")
    suspend fun confirmarRecuperacion(@Body solicitud: ConfirmarRecuperacionSolicitud)
}
