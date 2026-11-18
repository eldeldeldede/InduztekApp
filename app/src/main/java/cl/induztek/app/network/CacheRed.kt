package cl.induztek.app.network

import cl.induztek.app.local.AlmacenLocal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// =====================================================================
//  CacheRed.kt — "consulta con respaldo"
//  ---------------------------------------------------------------------
//  Con conexión: pide los datos al servidor y guarda una copia en el teléfono.
//  Sin conexión: entrega la última copia guardada (si existe) en vez de fallar.
//  Los errores del servidor (clave mala, permisos, etc.) NUNCA se tapan con la caché.
// =====================================================================
@PublishedApi
internal val gsonCache = Gson()

suspend inline fun <reified T> llamarConCache(clave: String, noinline llamada: suspend () -> T): ResultadoRed<T> {
    val resultado = llamarApi(llamada)
    return when (resultado) {
        is ResultadoRed.Exito -> {
            AlmacenLocal.guardarCache(clave, gsonCache.toJson(resultado.datos))
            resultado
        }
        ResultadoRed.SinConexion -> {
            val guardado = AlmacenLocal.leerCache(clave)
            if (guardado == null) resultado
            else try {
                ResultadoRed.Exito(gsonCache.fromJson<T>(guardado, object : TypeToken<T>() {}.type))
            } catch (e: Exception) {
                resultado
            }
        }
        is ResultadoRed.ErrorServidor -> resultado
    }
}
