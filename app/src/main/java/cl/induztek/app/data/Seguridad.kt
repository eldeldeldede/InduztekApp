package cl.induztek.app.data

import java.security.MessageDigest

// =====================================================================
//  Seguridad.kt — "hash" de contraseñas
//  ---------------------------------------------------------------------
//  El MER pide guardar `password_hash` y NUNCA la clave en texto plano.
//  Un hash es una huella irreversible: de "Induztek2026" se obtiene siempre el mismo
//  texto largo, pero desde ese texto no se puede volver a la clave.
//
//  Para iniciar sesión: se calcula el hash de lo que escribió el usuario y se compara
//  con el hash guardado en la base.
//
//  LIMITACIÓN (para decirla en la defensa): SHA-256 sin "sal" es suficiente para esta
//  entrega local, pero en la EFT, con backend, se debe usar un algoritmo pensado para
//  contraseñas (BCrypt o Argon2) en el servidor.
// =====================================================================
object Seguridad {
    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            // cada byte -> 2 caracteres hexadecimales ("%02x")
            .joinToString("") { "%02x".format(it) }
}
