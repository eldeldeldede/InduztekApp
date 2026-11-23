package cl.induztek.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// =====================================================================
//  EquiposApi.kt — los endpoints de equipos-service (puerto 8082)
//  `clienteId` hace que el servidor devuelva SOLO lo de esa empresa (así el cliente nunca ve datos ajenos).
// =====================================================================
interface EquiposApi {

    @GET("api/instalaciones")
    suspend fun instalaciones(@Query("clienteId") clienteId: Long?): List<InstalacionDto>

    @GET("api/instalaciones/{id}")
    suspend fun instalacion(@Path("id") id: Long, @Query("clienteId") clienteId: Long?): InstalacionDto

    @POST("api/instalaciones")
    suspend fun crearInstalacion(@Body solicitud: InstalacionSolicitud): InstalacionDto

    @PUT("api/instalaciones/{id}")
    suspend fun actualizarInstalacion(@Path("id") id: Long, @Body solicitud: InstalacionSolicitud): InstalacionDto

    @GET("api/equipos")
    suspend fun equipos(
        @Query("clienteId") clienteId: Long?,
        @Query("instalacionId") instalacionId: Long?
    ): List<EquipoDto>

    @GET("api/equipos/{id}")
    suspend fun equipo(@Path("id") id: Long, @Query("clienteId") clienteId: Long?): EquipoDto

    @POST("api/equipos")
    suspend fun crearEquipo(@Body solicitud: EquipoSolicitud): EquipoDto

    @PUT("api/equipos/{id}")
    suspend fun actualizarEquipo(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") id: Long,
        @Body solicitud: EquipoSolicitud
    ): EquipoDto

    @GET("api/comunas")
    suspend fun comunas(): List<ComunaDto>

    @GET("api/tipos-equipo")
    suspend fun tiposEquipo(): List<TipoEquipoDto>
}
