package cl.induztek.app.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// =====================================================================
//  ApiClient.kt — construye Retrofit UNA sola vez para toda la app
//  ---------------------------------------------------------------------
//  Retrofit convierte una interfaz Kotlin (UsuariosApi) en llamadas HTTP reales.
//  GsonConverterFactory transforma el JSON del servidor en las clases de Dtos.kt (y al revés).
//  Los tiempos de espera cortos hacen que, sin servidor o sin red, la app lo note rápido
//  y pase al modo sin conexión en vez de quedarse "pensando".
// =====================================================================
object ApiClient {

    private val clienteHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // `by lazy`: se crea la primera vez que alguien lo usa
    val usuarios: UsuariosApi by lazy { retrofit(ApiConfig.URL_USUARIOS).create(UsuariosApi::class.java) }
    val equipos: EquiposApi by lazy { retrofit(ApiConfig.URL_EQUIPOS).create(EquiposApi::class.java) }
    val protocolos: ProtocolosApi by lazy { retrofit(ApiConfig.URL_PROTOCOLOS).create(ProtocolosApi::class.java) }
    val reportes: ReportesApi by lazy { retrofit(ApiConfig.URL_REPORTES).create(ReportesApi::class.java) }
}
