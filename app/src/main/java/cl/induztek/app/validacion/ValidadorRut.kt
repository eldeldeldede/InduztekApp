package cl.induztek.app.validacion

object ValidadorRut {

    private fun limpiar(rut: String): String =
        rut.trim().uppercase().filter { it.isDigit() || it == 'K' }

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

    fun esValido(rut: String): Boolean {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()

        if (!cuerpo.all { it.isDigit() } || cuerpo.length !in 7..8) return false
        return calcularDigito(cuerpo) == dv
    }

    fun formatear(rut: String): String {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return rut.trim()
        val cuerpo = limpio.dropLast(1)
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-${limpio.last()}"
    }

    fun error(rut: String): String? = when {
        rut.isBlank() -> "El RUT es obligatorio"
        !esValido(rut) -> "RUT inválido (revisa el dígito verificador)"
        else -> null
    }
}
