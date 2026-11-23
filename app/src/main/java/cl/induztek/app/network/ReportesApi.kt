package cl.induztek.app.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

// =====================================================================
//  ReportesApi.kt — los endpoints de reportes-service (puerto 8084)
//  Es el servicio que arma el historial y las actas; si quien consulta es un CLIENTE, el servidor
//  filtra por su empresa y no le entrega borradores ni anulados (los no formalizados salen como pendientes).
// =====================================================================
interface ReportesApi {

    @GET("api/reportes/equipos/{id}/historial")
    suspend fun historialEquipo(
        @Header("X-Usuario-Id") idActor: Long,
        @Path("id") idEquipo: Long
    ): HistorialEquipoDto

    @GET("api/reportes/actas")
    suspend fun actas(
        @Header("X-Usuario-Id") idActor: Long,
        @Query("instalacionId") idInstalacion: Long?
    ): List<ActaDto>
}
