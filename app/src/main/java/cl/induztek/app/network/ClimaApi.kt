package cl.induztek.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// =====================================================================
//  ClimaApi.kt — API EXTERNA (Open-Meteo, gratuita y sin clave): clima actual de una coordenada.
//  A diferencia de los microservicios propios (que pasan por el gateway con http://10.0.2.2:8080),
//  esta se consume por https directo desde Internet y no guarda nada en nuestra base de datos.
//  GET https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=temperature_2m,relative_humidity_2m
// =====================================================================
interface ClimaApi {
    @GET("v1/forecast")
    suspend fun actual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") campos: String = "temperature_2m,relative_humidity_2m"
    ): ClimaDto
}

data class ClimaDto(
    val current: ClimaActualDto?
)

data class ClimaActualDto(
    @SerializedName("temperature_2m") val temperatura: Double?,
    @SerializedName("relative_humidity_2m") val humedad: Double?
)
