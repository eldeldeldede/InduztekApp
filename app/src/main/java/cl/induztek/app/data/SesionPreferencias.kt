package cl.induztek.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.Usuario

// =====================================================================
//  SesionPreferencias.kt — recuerda quién inició sesión (SharedPreferences)
//  ---------------------------------------------------------------------
//  SharedPreferences es el "localStorage" de Android: pares CLAVE -> VALOR simples.
//  Ahora que el login lo hace el servidor, el usuario ya no vive en SQLite, así que se guardan
//  aquí sus datos (NUNCA la clave). Hay DOS archivos:
//
//   "sesion"          -> el usuario conectado ahora. Se borra al cerrar sesión.
//   "ultimo_ingreso"  -> el último usuario que entró CON conexión + el hash de su clave.
//                        Sirve para poder entrar SIN conexión en terreno (RNF-02): se compara
//                        el hash de lo que escribe con el guardado. No se borra al cerrar sesión.
//
//  LIMITACIÓN (para la defensa): el hash SHA-256 guardado en el teléfono es una conveniencia
//  para el modo sin señal; la fuente de verdad de las claves sigue siendo el servidor.
// =====================================================================
class SesionPreferencias(context: Context) {

    // Usuario + hash de su clave, tal como quedó en el último ingreso con conexión
    data class UltimoIngreso(val usuario: Usuario, val hashClave: String)

    private val appContext = context.applicationContext
    private val prefsSesion: SharedPreferences = appContext.getSharedPreferences("sesion", Context.MODE_PRIVATE)
    private val prefsUltimo: SharedPreferences = appContext.getSharedPreferences("ultimo_ingreso", Context.MODE_PRIVATE)

    companion object {
        private const val ID = "id_usuario"
        private const val ID_ROL = "id_rol"
        private const val CODIGO_ROL = "codigo_rol"
        private const val RUT = "rut"
        private const val NOMBRE = "nombre"
        private const val CORREO = "correo"
        private const val TELEFONO = "telefono"
        private const val ACTIVO = "activo"
        private const val GIRO = "giro"
        private const val CONTACTO_NOMBRE = "contacto_nombre"
        private const val CONTACTO_CARGO = "contacto_cargo"
        private const val DEBE_CAMBIAR = "debe_cambiar_contrasena"
        private const val HASH = "hash_clave"
    }

    fun guardar(usuario: Usuario) {
        prefsSesion.edit { escribir(usuario) }
    }

    // null = no hay sesión iniciada
    fun leer(): Usuario? = leerUsuario(prefsSesion)

    fun borrar() {
        prefsSesion.edit { clear() }
    }

    fun guardarUltimoIngreso(usuario: Usuario, hashClave: String) {
        prefsUltimo.edit {
            escribir(usuario)
            putString(HASH, hashClave)
        }
    }

    fun leerUltimoIngreso(): UltimoIngreso? {
        val usuario = leerUsuario(prefsUltimo) ?: return null
        val hash = prefsUltimo.getString(HASH, null) ?: return null
        return UltimoIngreso(usuario, hash)
    }

    // ---------- Conversión Usuario <-> pares clave/valor ----------
    private fun SharedPreferences.Editor.escribir(u: Usuario) {
        putLong(ID, u.idUsuario)
        putLong(ID_ROL, u.idRol)
        putString(CODIGO_ROL, u.codigoRol.codigo)
        putString(RUT, u.rut)
        putString(NOMBRE, u.nombre)
        putString(CORREO, u.correo)
        putString(TELEFONO, u.telefono)
        putBoolean(ACTIVO, u.activo)
        putString(GIRO, u.giro)
        putString(CONTACTO_NOMBRE, u.contactoNombre)
        putString(CONTACTO_CARGO, u.contactoCargo)
        putBoolean(DEBE_CAMBIAR, u.debeCambiarContrasena)
    }

    private fun leerUsuario(prefs: SharedPreferences): Usuario? {
        if (!prefs.contains(ID)) return null
        val codigo = prefs.getString(CODIGO_ROL, null) ?: return null
        val rol = CodigoRol.entries.firstOrNull { it.codigo == codigo } ?: return null
        return Usuario(
            idUsuario = prefs.getLong(ID, 0L),
            idRol = prefs.getLong(ID_ROL, rol.id),
            codigoRol = rol,
            rut = prefs.getString(RUT, "") ?: "",
            nombre = prefs.getString(NOMBRE, "") ?: "",
            correo = prefs.getString(CORREO, "") ?: "",
            telefono = prefs.getString(TELEFONO, null),
            passwordHash = "",
            activo = prefs.getBoolean(ACTIVO, true),
            giro = prefs.getString(GIRO, null),
            contactoNombre = prefs.getString(CONTACTO_NOMBRE, null),
            contactoCargo = prefs.getString(CONTACTO_CARGO, null),
            debeCambiarContrasena = prefs.getBoolean(DEBE_CAMBIAR, false)
        )
    }
}
