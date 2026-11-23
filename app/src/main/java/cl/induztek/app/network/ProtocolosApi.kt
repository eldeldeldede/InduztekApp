package cl.induztek.app.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// =====================================================================
//  ProtocolosApi.kt — los endpoints de protocolos-service (puerto 8083)
//  "X-Usuario-Id" indica quién realiza la acción (el servidor valida el rol).
// =====================================================================
interface ProtocolosApi {

    @GET("api/visitas")
    suspend fun visitas(@Query("instalacionId") instalacionId: Long?): List<VisitaDto>

    @GET("api/visitas/{id}")
    suspend fun visita(@Path("id") id: Long): VisitaDto

    @GET("api/visitas")
    suspend fun visitaPorOt(@Query("buscar") numeroOt: String): List<VisitaDto>

    @POST("api/visitas")
    suspend fun crearVisita(@Header("X-Usuario-Id") idActor: Long, @Body solicitud: VisitaSolicitud): VisitaDto

    @POST("api/visitas/{id}/cierre")
    suspend fun cerrarVisita(@Header("X-Usuario-Id") idActor: Long, @Path("id") id: Long): VisitaDto

    @POST("api/visitas/{id}/recepcion")
    suspend fun registrarRecepcion(@Path("id") id: Long, @Body solicitud: RecepcionSolicitud): VisitaDto

    @GET("api/protocolos")
    suspend fun protocolos(
        @Query("visitaId") visitaId: Long?,
        @Query("equipoId") equipoId: Long?
    ): List<ProtocoloDto>

    @GET("api/protocolos/{id}")
    suspend fun protocolo(@Path("id") id: Long): ProtocoloDto

    @POST("api/protocolos")
    suspend fun crearProtocolo(@Header("X-Usuario-Id") idActor: Long, @Body solicitud: ProtocoloSolicitud): ProtocoloDto

    @PUT("api/protocolos/{id}")
    suspend fun actualizarProtocolo(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") id: Long,
        @Body solicitud: ProtocoloActualizacionSolicitud
    ): ProtocoloDto

    @POST("api/protocolos/{id}/pruebas")
    suspend fun registrarPrueba(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") id: Long,
        @Body solicitud: PruebaSolicitud
    ): PruebaDto

    @DELETE("api/protocolos/{id}/pruebas/{idPrueba}")
    suspend fun eliminarPrueba(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") id: Long,
        @Path("idPrueba") idPrueba: Long
    )

    @POST("api/protocolos/{id}/registrar")
    suspend fun registrarEnTerreno(@Header("X-Usuario-Id") idActor: Long, @Path("id") id: Long): ProtocoloDto

    @POST("api/protocolos/{id}/formalizar")
    suspend fun formalizar(@Header("X-Usuario-Id") idActor: Long, @Path("id") id: Long): ProtocoloDto

    @POST("api/protocolos/{id}/observar")
    suspend fun observar(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") id: Long,
        @Body solicitud: MotivoSolicitud
    ): ProtocoloDto

    @POST("api/protocolos/{id}/anular")
    suspend fun anular(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") id: Long,
        @Body solicitud: MotivoSolicitud
    ): ProtocoloDto

    @GET("api/instrumentos")
    suspend fun instrumentos(@Query("soloVigentes") soloVigentes: Boolean?): List<InstrumentoDto>

    @GET("api/tipos-prueba")
    suspend fun tiposPrueba(@Query("tipoEquipoId") tipoEquipoId: Long?): List<TipoPruebaDto>
}
