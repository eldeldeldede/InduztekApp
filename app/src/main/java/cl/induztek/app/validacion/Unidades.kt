package cl.induztek.app.validacion

enum class UnidadAislacion(val simbolo: String, val factorAMOhm: Double) {
    OHM("Ω", 0.000001),
    KOHM("kΩ", 0.001),
    MOHM("MΩ", 1.0),
    GOHM("GΩ", 1000.0),
    TOHM("TΩ", 1_000_000.0)
}

enum class UnidadBaja(val simbolo: String, val factorAUOhm: Double) {
    UOHM("µΩ", 1.0),
    MOHM("mΩ", 1_000.0),
    OHM("Ω", 1_000_000.0)
}

object Unidades {

    fun aMegaOhm(valor: Double, unidad: UnidadAislacion): Double = valor * unidad.factorAMOhm

    fun aMicroOhm(valor: Double, unidad: UnidadBaja): Double = valor * unidad.factorAUOhm

    fun aMiliOhm(valor: Double, unidad: UnidadBaja): Double = aMicroOhm(valor, unidad) / 1_000.0

    fun parsearDecimal(texto: String): Double? {
        val limpio = texto.trim().replace(',', '.')
        if (limpio.isEmpty()) return null
        val numero = limpio.toDoubleOrNull() ?: return null

        return if (numero.isNaN() || numero.isInfinite()) null else numero
    }

    fun formatear(valor: Double, decimales: Int = 2): String {
        val texto = String.format(java.util.Locale.US, "%.${decimales}f", valor)
        val sinCeros = if (texto.contains('.')) texto.trimEnd('0').trimEnd('.') else texto
        return sinCeros.replace('.', ',')
    }

    fun formatearPorcentaje(valor: Double): String =
        String.format(java.util.Locale.US, "%+.1f", valor).replace('.', ',') + " %"
}
