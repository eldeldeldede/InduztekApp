package cl.induztek.app.validacion

import java.time.LocalDate

// =====================================================================
//  ValidacionesFormulario.kt — las validaciones de los CAMPOS de los formularios
//  ---------------------------------------------------------------------
//  En MiRegistro, FormularioViewModel.onRegistrar() tenía las comprobaciones escritas
//  adentro (nombre en blanco, correo sin "@", contraseña de 6 caracteres). Aquí hacemos
//  lo mismo, pero juntas en un objeto para que cada ViewModel las reutilice:
//
//      val errorTag = ValidacionesFormulario.tag(estado.tag)       // String? como errorNombre
//
//  Cada función devuelve el mensaje de error o null si el campo está bien.
//  Los campos numéricos llegan como TEXTO (lo que escribe el usuario en el TextField).
// =====================================================================
object ValidacionesFormulario {

    // ---------- Login ----------
    fun correo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        // Un patrón simple: algo@algo.algo, sin espacios. (Suficiente para este proyecto.)
        !Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(correo.trim()) -> "Ingresa un correo válido"
        else -> null
    }

    fun clave(clave: String): String? = when {
        clave.isEmpty() -> "La contraseña es obligatoria"
        else -> null
    }

    // ---------- Equipo (alta) ----------

    // TAG: lo escribe el técnico mirando la placa ("TR-01", "CEL-MT-03"). Solo letras,
    // números, guion, punto y guion bajo. RN-01 (que no se repita en la instalación) lo
    // revisa la base al insertar.
    fun tag(tag: String): String? = when {
        tag.isBlank() -> "El TAG es obligatorio"
        tag.trim().length > 20 -> "Máximo 20 caracteres"
        !Regex("^[A-Za-z0-9._-]+$").matches(tag.trim()) -> "Solo letras, números, guion, punto o guion bajo"
        else -> null
    }

    // Año de fabricación: opcional. Si se escribe, entre 1900 y el año actual.
    fun anioFabricacion(texto: String, anioActual: Int = LocalDate.now().year): String? {
        if (texto.isBlank()) return null
        val anio = texto.trim().toIntOrNull() ?: return "Ingresa un año válido"
        return if (anio !in 1900..anioActual) "El año debe estar entre 1900 y $anioActual" else null
    }

    // Dato de placa numérico OPCIONAL y positivo (potencia, tensiones, corriente...).
    fun numeroPositivoOpcional(texto: String): String? {
        if (texto.isBlank()) return null
        val numero = Unidades.parsearDecimal(texto) ?: return "Ingresa un número válido"
        return if (numero <= 0.0) "Debe ser mayor que cero" else null
    }

    // ---------- Protocolo: condiciones ambientales (HU-02) ----------
    fun temperatura(texto: String): String? {
        val valor = Unidades.parsearDecimal(texto) ?: return "Ingresa la temperatura en °C"
        return if (valor !in -30.0..60.0) "Temperatura fuera de rango (-30 a 60 °C)" else null
    }

    // Coincide con el CHECK del esquema: humedad entre 0 y 100.
    fun humedad(texto: String): String? {
        val valor = Unidades.parsearDecimal(texto) ?: return "Ingresa la humedad en %"
        return if (valor !in 0.0..100.0) "La humedad debe estar entre 0 y 100 %" else null
    }

    // ---------- Mediciones ----------

    // Una medición: obligatoria, numérica y no negativa (una resistencia no puede ser negativa).
    fun medicion(texto: String, nombre: String): String? {
        val valor = Unidades.parsearDecimal(texto) ?: return "Ingresa $nombre"
        return if (valor < 0.0) "$nombre no puede ser negativo" else null
    }

    // Motivo de "No realizada" (Aclaración 2), de observación o de anulación.
    // Valores que se usan como divisor o que físicamente no pueden ser cero (relación, tiempo...)
    fun medicionPositiva(texto: String, nombre: String): String? {
        val valor = Unidades.parsearDecimal(texto) ?: return "Ingresa $nombre"
        return if (valor <= 0.0) "$nombre debe ser mayor que cero" else null
    }

    fun medicionEnRango(texto: String, nombre: String, minimo: Double, maximo: Double, unidad: String): String? {
        val valor = Unidades.parsearDecimal(texto) ?: return "Ingresa $nombre"
        return if (valor !in minimo..maximo)
            "$nombre fuera de rango (${Unidades.formatear(minimo)} a ${Unidades.formatear(maximo)} $unidad)"
        else null
    }

    // Posición del cambiador de tomas de un transformador: número entero (típicamente 1 a 33)
    fun posicionCambiador(texto: String): String? {
        val posicion = texto.trim().toIntOrNull() ?: return "Ingresa la posición como número entero"
        return if (posicion !in 1..33) "La posición debe estar entre 1 y 33" else null
    }

    fun motivo(texto: String): String? = when {
        texto.isBlank() -> "El motivo es obligatorio"
        texto.trim().length < 5 -> "Describe el motivo (mínimo 5 caracteres)"
        else -> null
    }

    // ---------- Visita ----------
    // Formato de la orden de trabajo: "OT-2026-1187"
    fun numeroOt(texto: String): String? = when {
        texto.isBlank() -> "El número de OT es obligatorio"
        !Regex("^OT-\\d{4}-\\d{3,5}$").matches(texto.trim().uppercase()) -> "Formato esperado: OT-2026-1187"
        else -> null
    }

    // ---------- Recepción del acta (HU-08) ----------
    fun nombreReceptor(texto: String): String? =
        if (texto.trim().length < 3) "Ingresa el nombre de quien recibe" else null

    fun cargoReceptor(texto: String): String? =
        if (texto.isBlank()) "Ingresa el cargo de quien recibe" else null

    // ---------- Cambio de contraseña (primer ingreso) ----------
    // Mismas reglas que usuarios-service: mínimo 6 caracteres y distinta de la actual.
    fun claveNueva(nueva: String, actual: String): String? = when {
        nueva.isEmpty() -> "La nueva contraseña es obligatoria"
        nueva.length < 6 -> "Debe tener al menos 6 caracteres"
        nueva == actual -> "Debe ser distinta de la contraseña actual"
        else -> null
    }

    fun confirmacionClave(nueva: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Repite la nueva contraseña"
        confirmacion != nueva -> "Las contraseñas no coinciden"
        else -> null
    }

    // ---------- Crear usuario (solo Jefe técnico) ----------
    fun nombreUsuario(texto: String): String? =
        if (texto.trim().length < 3) "Ingresa el nombre (mínimo 3 caracteres)" else null

    // El teléfono es opcional: si viene, 8 a 12 dígitos (puede llevar + y espacios).
    fun telefono(texto: String): String? {
        if (texto.isBlank()) return null
        val digitos = texto.count { it.isDigit() }
        return if (digitos !in 8..12 || !texto.all { it.isDigit() || it in "+ -" }) "Teléfono inválido" else null
    }
}
