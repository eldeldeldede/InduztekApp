package cl.induztek.app.network

// =====================================================================
//  ApiConfig.kt — direcciones de los microservicios
//  ---------------------------------------------------------------------
//  10.0.2.2 es la dirección que usa el EMULADOR de Android para llegar al PC donde corre
//  (Laragon + API Gateway + microservicios). Dentro del emulador, "localhost" sería el propio emulador.
//  Si algún día se prueba con un teléfono físico, se cambia SOLO esta línea por la IP del PC
//  (ej. "192.168.1.50") y se permite esa IP en res/xml/network_security_config.xml.
// =====================================================================
object ApiConfig {
    private const val HOST = "10.0.2.2"
    private const val URL_GATEWAY = "http://$HOST:8080/"

    const val URL_USUARIOS = URL_GATEWAY
    const val URL_EQUIPOS = URL_GATEWAY
    const val URL_PROTOCOLOS = URL_GATEWAY
    const val URL_REPORTES = URL_GATEWAY

    const val URL_CLIMA = "https://api.open-meteo.com/"
    const val LATITUD_CLIMA = -33.45
    const val LONGITUD_CLIMA = -70.66
}
