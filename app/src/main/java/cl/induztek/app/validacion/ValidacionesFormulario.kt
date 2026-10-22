package cl.induztek.app.validacion

import java.time.LocalDate

object ValidacionesFormulario {

    fun correo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        !Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(correo.trim()) -> "Ingresa un correo válido"
        else -> null
    }

    fun clave(clave: String): String? = when {
        clave.isEmpty() -> "La contraseña es obligatoria"
        else -> null
    }

    fun tag(tag: String): String? = when {
        tag.isBlank() -> "El TAG es obligatorio"
        tag.trim().length > 20 -> "Máximo 20 caracteres"
        !Regex("^[A-Za-z0-9._-]+$").matches(tag.trim()) -> "Solo letras, números, guion, punto o guion bajo"
        else -> null
    }

    fun anioFabricacion(texto: String, anioActual: Int = LocalDate.now().year): String? {
        if (texto.isBlank()) return null
        val anio = texto.trim().toIntOrNull() ?: return "Ingresa un año válido"
        return if (anio !in 1900..anioActual) "El año debe estar entre 1900 y $anioActual" else null
    }

    fun numeroPositivoOpcional(texto: String): String? {
        if (texto.isBlank()) return null
        val numero = Unidades.parsearDecimal(texto) ?: return "Ingresa un número válido"
        return if (numero <= 0.0) "Debe ser mayor que cero" else null
    }

    fun temperatura(texto: String): String? {
        val valor = Unidades.parsearDecimal(texto) ?: return "Ingresa la temperatura en °C"
        return if (valor !in -30.0..60.0) "Temperatura fuera de rango (-30 a 60 °C)" else null
    }

    fun humedad(texto: String): String? {
        val valor = Unidades.parsearDecimal(texto) ?: return "Ingresa la humedad en %"
        return if (valor !in 0.0..100.0) "La humedad debe estar entre 0 y 100 %" else null
    }

    fun medicion(texto: String, nombre: String): String? {
        val valor = Unidades.parsearDecimal(texto) ?: return "Ingresa $nombre"
        return if (valor < 0.0) "$nombre no puede ser negativo" else null
    }

    fun motivo(texto: String): String? = when {
        texto.isBlank() -> "El motivo es obligatorio"
        texto.trim().length < 5 -> "Describe el motivo (mínimo 5 caracteres)"
        else -> null
    }

    fun numeroOt(texto: String): String? = when {
        texto.isBlank() -> "El número de OT es obligatorio"
        !Regex("^OT-\\d{4}-\\d{3,5}$").matches(texto.trim().uppercase()) -> "Formato esperado: OT-2026-1187"
        else -> null
    }

    fun nombreReceptor(texto: String): String? =
        if (texto.trim().length < 3) "Ingresa el nombre de quien recibe" else null

    fun cargoReceptor(texto: String): String? =
        if (texto.isBlank()) "Ingresa el cargo de quien recibe" else null
}