package cl.induztek.app.data

import android.content.Context
import androidx.core.content.edit

// =====================================================================
//  SesionPreferencias.kt — recuerda quién inició sesión (SharedPreferences)
//  ---------------------------------------------------------------------
//  Igual que en MiRegistro: SharedPreferences es el "localStorage" de Android y guarda
//  pares CLAVE -> VALOR simples. Aquí guardamos SOLO el id del usuario (nunca la clave).
//  Al abrir la app se lee ese id y se busca el usuario en SQLite (InduztekDbHelper.buscarUsuarioPorId).
//
//   - Muchos registros con la misma estructura (equipos, protocolos) -> SQLite
//   - Un dato suelto (quién inició sesión)                          -> SharedPreferences
// =====================================================================
class SesionPreferencias(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val SIN_SESION = -1L
    }

    fun guardar(idUsuario: Long) {
        prefs.edit { putLong(CLAVE_ID_USUARIO, idUsuario) }
    }

    // -1 significa "no hay sesión iniciada".
    fun leer(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    fun hayDatosDeSesion(): Boolean = leer() != SIN_SESION

    fun borrar() {
        prefs.edit { remove(CLAVE_ID_USUARIO) }
    }
}
