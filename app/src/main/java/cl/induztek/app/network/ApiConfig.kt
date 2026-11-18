package cl.induztek.app.network

// =====================================================================
//  ApiConfig.kt — direcciones de los microservicios
//  ---------------------------------------------------------------------
//  10.0.2.2 es la dirección que usa el EMULADOR de Android para llegar al PC donde corre
//  (Laragon + los 4 microservicios). Dentro del emulador, "localhost" sería el propio emulador.
//  Si algún día se prueba con un teléfono físico, se cambia SOLO esta línea por la IP del PC
//  (ej. "192.168.1.50") y se permite esa IP en res/xml/network_security_config.xml.
// =====================================================================
object ApiConfig {
    private const val HOST = "10.0.2.2"

    const val URL_USUARIOS = "http://$HOST:8081/"
    const val URL_EQUIPOS = "http://$HOST:8082/"
    const val URL_PROTOCOLOS = "http://$HOST:8083/"
    const val URL_REPORTES = "http://$HOST:8084/"
}
