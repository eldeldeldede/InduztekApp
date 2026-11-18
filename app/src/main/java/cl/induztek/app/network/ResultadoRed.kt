package cl.induztek.app.network

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

// =====================================================================
//  ResultadoRed.kt — el resultado de una llamada al servidor, en 3 casos posibles
//  ---------------------------------------------------------------------
//   Exito          -> el servidor respondió bien y trae los datos
//   ErrorServidor  -> el servidor respondió con un error (clave mala, RUT repetido...)
//                     `mensaje` es el texto que el microservicio manda en español
//   SinConexion    -> no se pudo llegar al servidor (sin red, servicio apagado, tiempo agotado)
//
//  Separar "error del servidor" de "sin conexión" es la base del modo offline:
//  solo en SinConexion la app recurre a los datos guardados en el teléfono.
// =====================================================================
sealed interface ResultadoRed<out T> {
    data class Exito<T>(val datos: T) : ResultadoRed<T>
    data class ErrorServidor(val codigo: Int, val mensaje: String) : ResultadoRed<Nothing>
    data object SinConexion : ResultadoRed<Nothing>
}

// Ejecuta una llamada de Retrofit y traduce lo que pase a un ResultadoRed (nunca lanza excepciones de red).
suspend fun <T> llamarApi(llamada: suspend () -> T): ResultadoRed<T> =
    try {
        ResultadoRed.Exito(llamada())
    } catch (e: HttpException) {
        ResultadoRed.ErrorServidor(e.code(), mensajeDeError(e))
    } catch (e: IOException) {
        ResultadoRed.SinConexion
    } catch (e: JsonParseException) {
        ResultadoRed.ErrorServidor(0, "Respuesta inesperada del servidor")
    }

// Los microservicios responden los errores como JSON {"status":400,"error":"...","message":"RUT ya registrado"}.
// Se extrae "message"; si no viene, se usa un texto según el código.
internal fun mensajeDeError(e: HttpException): String {
    val cuerpo = try { e.response()?.errorBody()?.string() } catch (ignorada: IOException) { null }
    return mensajeDesdeJson(cuerpo) ?: mensajePorCodigo(e.code())
}

internal fun mensajeDesdeJson(json: String?): String? {
    if (json.isNullOrBlank()) return null
    return try {
        val mensaje = JsonParser.parseString(json).asJsonObject.get("message")
        if (mensaje == null || mensaje.isJsonNull) null else mensaje.asString.takeIf { it.isNotBlank() }
    } catch (ignorada: JsonParseException) {
        null
    } catch (ignorada: IllegalStateException) {
        null
    }
}

internal fun mensajePorCodigo(codigo: Int): String = when (codigo) {
    400 -> "Los datos enviados no son válidos"
    401 -> "Correo o contraseña incorrectos"
    403 -> "No tienes permiso para esta acción"
    404 -> "No se encontró lo solicitado"
    409 -> "Ya existe un registro con esos datos"
    in 500..599 -> "El servidor tuvo un problema. Intenta de nuevo"
    else -> "Error al comunicarse con el servidor ($codigo)"
}
