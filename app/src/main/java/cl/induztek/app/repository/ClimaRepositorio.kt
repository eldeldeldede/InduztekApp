package cl.induztek.app.repository

import cl.induztek.app.model.CondicionClima
import cl.induztek.app.network.ApiClient
import cl.induztek.app.network.ApiConfig
import cl.induztek.app.network.ClimaDto
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.llamarApi
import com.google.gson.JsonParseException
import kotlin.math.roundToInt

// =====================================================================
//  ClimaRepositorio — consulta la API externa de clima y la convierte al modelo de la app
// =====================================================================
class ClimaRepositorio {

    suspend fun actual(): ResultadoRed<CondicionClima> = llamarApi {
        ApiClient.clima.actual(ApiConfig.LATITUD_CLIMA, ApiConfig.LONGITUD_CLIMA).aCondicion()
            ?: throw JsonParseException("Respuesta del clima sin datos")
    }
}

// Temperatura con 1 decimal y humedad entera (como se anotan en el formulario); null si falta algún dato.
fun ClimaDto.aCondicion(): CondicionClima? {
    val temperatura = current?.temperatura ?: return null
    val humedad = current.humedad ?: return null
    return CondicionClima(
        temperatura = (temperatura * 10).roundToInt() / 10.0,
        humedad = humedad.roundToInt().toDouble()
    )
}
