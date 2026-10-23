package cl.induztek.app.validacion

// =====================================================================
//  Unidades.kt — RNF-05: "los valores se guardan SIEMPRE en la unidad base".
//  ---------------------------------------------------------------------
//  El problema real (Anexo 12): el técnico anotaba "5,8 G" y después nadie sabía si eran
//  GΩ o MΩ. La app resuelve esto así: el técnico elige la unidad en un menú y AQUÍ se
//  convierte a la unidad base del parámetro antes de guardar (ej.: 5,8 GΩ -> 5800 MΩ).
// =====================================================================

// Unidades de resistencia de AISLACIÓN. Base de la base de datos: MΩ.
enum class UnidadAislacion(val simbolo: String, val factorAMOhm: Double) {
    OHM("Ω", 0.000001),
    KOHM("kΩ", 0.001),
    MOHM("MΩ", 1.0),
    GOHM("GΩ", 1000.0),
    TOHM("TΩ", 1_000_000.0)
}

// Unidades para resistencia de CONTACTOS (µΩ, base) y DEVANADOS (mΩ, base).
enum class UnidadBaja(val simbolo: String, val factorAUOhm: Double) {
    UOHM("µΩ", 1.0),
    MOHM("mΩ", 1_000.0),
    OHM("Ω", 1_000_000.0)
}

object Unidades {

    // 5.8 GΩ -> 5800.0 MΩ
    fun aMegaOhm(valor: Double, unidad: UnidadAislacion): Double = valor * unidad.factorAMOhm

    // 0.085 mΩ -> 85.0 µΩ
    fun aMicroOhm(valor: Double, unidad: UnidadBaja): Double = valor * unidad.factorAUOhm

    // Para guardar un valor de resistencia de DEVANADOS (base: mΩ) escrito en cualquier unidad baja.
    fun aMiliOhm(valor: Double, unidad: UnidadBaja): Double = aMicroOhm(valor, unidad) / 1_000.0

    // Convierte lo que escribe el técnico a número. En Chile se escribe con coma: "5,8".
    // Devuelve null si el texto no es un número (para mostrar "Ingresa un número válido").
    fun parsearDecimal(texto: String): Double? {
        val limpio = texto.trim().replace(',', '.')
        if (limpio.isEmpty()) return null
        val numero = limpio.toDoubleOrNull() ?: return null
        // "NaN" e "Infinity" sí los acepta toDoubleOrNull: no son mediciones.
        return if (numero.isNaN() || numero.isInfinite()) null else numero
    }

    // Muestra un valor con los decimales que importan, sin "5800.0" (se ve "5800").
    fun formatear(valor: Double, decimales: Int = 2): String {
        val texto = String.format(java.util.Locale.US, "%.${decimales}f", valor)
        val sinCeros = if (texto.contains('.')) texto.trimEnd('0').trimEnd('.') else texto
        return sinCeros.replace('.', ',')
    }

    // Porcentaje con signo y una decimal: -37.755 -> "-37,8 %", 12.0 -> "+12,0 %"
    fun formatearPorcentaje(valor: Double): String =
        String.format(java.util.Locale.US, "%+.1f", valor).replace('.', ',') + " %"
}
