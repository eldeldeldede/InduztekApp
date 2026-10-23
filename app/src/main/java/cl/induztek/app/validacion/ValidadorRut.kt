package cl.induztek.app.validacion

// =====================================================================
//  ValidadorRut.kt — RN-03: "Los RUT (de clientes, técnicos y receptores) deben tener
//  un dígito verificador válido."
//  ---------------------------------------------------------------------
//  Kotlin PURO: no usa Android, así que se puede probar con un test unitario normal
//  (carpeta app/src/test), sin emulador. Es la misma idea que las validaciones del
//  FormularioViewModel de MiRegistro ("correo válido", "mínimo 6 caracteres"), pero
//  aquí vive aparte para que la usen VARIAS pantallas (alta de cliente, acta de recepción...).
//
//  ¿Cómo se calcula el dígito verificador (módulo 11)?
//    RUT 76.482.915-8 -> cuerpo 76482915
//    1) Se recorren los dígitos del cuerpo de DERECHA a IZQUIERDA.
//    2) Se multiplican por 2,3,4,5,6,7 y se repite (2,3,4...).
//    3) Se suman los productos.
//    4) dv = 11 - (suma % 11).  Si da 11 -> "0".  Si da 10 -> "K".
// =====================================================================
object ValidadorRut {

    // Deja solo dígitos y K: "76.482.915-8" -> "764829158"
    private fun limpiar(rut: String): String =
        rut.trim().uppercase().filter { it.isDigit() || it == 'K' }

    // Calcula el dígito verificador de un cuerpo numérico ("76482915" -> '8')
    fun calcularDigito(cuerpo: String): Char {
        var suma = 0
        var factor = 2
        for (digito in cuerpo.reversed()) {
            suma += (digito - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + resto)
        }
    }

    // true si el RUT tiene formato correcto Y el dígito verificador coincide.
    // Acepta con o sin puntos y guion: "76.482.915-8", "76482915-8" y "764829158".
    fun esValido(rut: String): Boolean {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        // El cuerpo debe ser solo números (una K en medio es inválida) y tener 7 u 8 dígitos
        // (RUT de personas y empresas: 1.000.000 a 99.999.999).
        if (!cuerpo.all { it.isDigit() } || cuerpo.length !in 7..8) return false
        return calcularDigito(cuerpo) == dv
    }

    // "764829158" -> "76.482.915-8". Si el texto no es un RUT razonable, lo devuelve igual.
    fun formatear(rut: String): String {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return rut.trim()
        val cuerpo = limpio.dropLast(1)
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-${limpio.last()}"
    }

    // Mensaje listo para mostrar bajo el campo (como errorCorreo en MiRegistro).
    // null = no hay error.
    fun error(rut: String): String? = when {
        rut.isBlank() -> "El RUT es obligatorio"
        !esValido(rut) -> "RUT inválido (revisa el dígito verificador)"
        else -> null
    }
}
